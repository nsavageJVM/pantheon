/*
 * Copyright 2018 ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package tech.pegasys.pantheon.consensus.ibft.statemachine;

import static tech.pegasys.pantheon.consensus.ibft.statemachine.IbftBlockHeightManager.MessageAge.CURRENT_ROUND;
import static tech.pegasys.pantheon.consensus.ibft.statemachine.IbftBlockHeightManager.MessageAge.FUTURE_ROUND;
import static tech.pegasys.pantheon.consensus.ibft.statemachine.IbftBlockHeightManager.MessageAge.PRIOR_ROUND;

import tech.pegasys.pantheon.consensus.ibft.BlockTimer;
import tech.pegasys.pantheon.consensus.ibft.ConsensusRoundIdentifier;
import tech.pegasys.pantheon.consensus.ibft.RoundTimer;
import tech.pegasys.pantheon.consensus.ibft.ibftevent.RoundExpiry;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.CommitPayload;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.MessageFactory;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.NewRoundPayload;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.Payload;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.PreparePayload;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.PreparedCertificate;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.ProposalPayload;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.RoundChangeCertificate;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.RoundChangePayload;
import tech.pegasys.pantheon.consensus.ibft.ibftmessagedata.SignedData;
import tech.pegasys.pantheon.consensus.ibft.validation.MessageValidatorFactory;
import tech.pegasys.pantheon.consensus.ibft.validation.NewRoundMessageValidator;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;

import java.time.Clock;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.common.collect.Maps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Responsible for starting/clearing Consensus rounds at a given block height. One of these is
 * created when a new block is imported to the chain. It immediately then creates a Round-0 object,
 * and sends a Proposal message. If the round times out prior to importing a block, this class is
 * responsible for creating a RoundChange message and transmitting it.
 */
public class IbftBlockHeightManager {

  protected enum MessageAge {
    PRIOR_ROUND,
    CURRENT_ROUND,
    FUTURE_ROUND
  }

  private static final Logger LOG = LogManager.getLogger();

  private final IbftRoundFactory roundFactory;
  private final RoundChangeManager roundChangeManager;
  private final BlockHeader parentHeader;
  private final RoundTimer roundTimer;
  private final BlockTimer blockTimer;
  private final IbftMessageTransmitter transmitter;
  private final MessageFactory messageFactory;
  private final Map<Integer, RoundState> futureRoundStateBuffer = Maps.newHashMap();
  private final NewRoundMessageValidator newRoundMessageValidator;
  private final Clock clock;
  private final Function<ConsensusRoundIdentifier, RoundState> roundStateCreator;
  private final IbftFinalState finalState;

  private Optional<PreparedCertificate> latestPreparedCertificate = Optional.empty();

  private IbftRound currentRound;

  public IbftBlockHeightManager(
      final BlockHeader parentHeader,
      final IbftFinalState finalState,
      final RoundChangeManager roundChangeManager,
      final IbftRoundFactory ibftRoundFactory,
      final Clock clock,
      final MessageValidatorFactory messageValidatorFactory) {
    this.parentHeader = parentHeader;
    this.roundFactory = ibftRoundFactory;
    this.roundTimer = finalState.getRoundTimer();
    this.blockTimer = finalState.getBlockTimer();
    this.transmitter = finalState.getTransmitter();
    this.messageFactory = finalState.getMessageFactory();
    this.clock = clock;
    this.roundChangeManager = roundChangeManager;
    this.finalState = finalState;

    newRoundMessageValidator = messageValidatorFactory.createNewRoundValidator(parentHeader);

    roundStateCreator =
        (roundIdentifier) ->
            new RoundState(
                roundIdentifier,
                finalState.getQuorumSize(),
                messageValidatorFactory.createMessageValidator(roundIdentifier, parentHeader));
  }

  public void start() {
    startNewRound(0);
    if (finalState.isLocalNodeProposerForRound(currentRound.getRoundIdentifier())) {
      blockTimer.startTimer(currentRound.getRoundIdentifier(), parentHeader);
    }
  }

  public void handleBlockTimerExpiry(final ConsensusRoundIdentifier roundIdentifier) {
    if (roundIdentifier.equals(currentRound.getRoundIdentifier())) {
      currentRound.createAndSendProposalMessage(clock.millis() / 1000);
    } else {
      LOG.info(
          "Block timer expired for a round ({}) other than current ({})",
          roundIdentifier,
          currentRound.getRoundIdentifier());
    }
  }

  public void roundExpired(final RoundExpiry expire) {
    if (!expire.getView().equals(currentRound.getRoundIdentifier())) {
      LOG.debug("Ignoring Round timer expired which does not match current round.");
      return;
    }

    LOG.info("Round has expired, creating PreparedCertificate and notifying peers.");
    final Optional<PreparedCertificate> preparedCertificate =
        currentRound.createPrepareCertificate();

    if (preparedCertificate.isPresent()) {
      latestPreparedCertificate = preparedCertificate;
    }

    startNewRound(currentRound.getRoundIdentifier().getRoundNumber() + 1);

    final SignedData<RoundChangePayload> localRoundChange =
        messageFactory.createSignedRoundChangePayload(
            currentRound.getRoundIdentifier(), latestPreparedCertificate);
    transmitter.multicastRoundChange(currentRound.getRoundIdentifier(), latestPreparedCertificate);

    // Its possible the locally created RoundChange triggers the transmission of a NewRound
    // message - so it must be handled accordingly.
    handleRoundChangePayload(localRoundChange);
  }

  public void handleProposalPayload(final SignedData<ProposalPayload> signedPayload) {
    LOG.info("Received a Proposal Payload.");
    actionOrBufferMessage(
        signedPayload, currentRound::handleProposalMessage, RoundState::setProposedBlock);
  }

  public void handlePreparePayload(final SignedData<PreparePayload> signedPayload) {
    LOG.info("Received a prepare Payload.");
    actionOrBufferMessage(
        signedPayload, currentRound::handlePrepareMessage, RoundState::addPrepareMessage);
  }

  public void handleCommitPayload(final SignedData<CommitPayload> payload) {
    LOG.info("Received a commit Payload.");
    actionOrBufferMessage(payload, currentRound::handleCommitMessage, RoundState::addCommitMessage);
  }

  private <T extends Payload> void actionOrBufferMessage(
      final SignedData<T> msgData,
      final Consumer<SignedData<T>> inRoundHandler,
      final BiConsumer<RoundState, SignedData<T>> buffer) {
    final Payload payload = msgData.getPayload();
    final MessageAge messageAge = determineAgeOfPayload(payload);
    if (messageAge == CURRENT_ROUND) {
      inRoundHandler.accept(msgData);
    } else if (messageAge == FUTURE_ROUND) {
      final ConsensusRoundIdentifier msgRoundId = payload.getRoundIdentifier();
      final RoundState roundstate =
          futureRoundStateBuffer.computeIfAbsent(
              msgRoundId.getRoundNumber(), k -> roundStateCreator.apply(msgRoundId));
      buffer.accept(roundstate, msgData);
    }
  }

  public void handleRoundChangePayload(final SignedData<RoundChangePayload> signedPayload) {
    final Optional<RoundChangeCertificate> result =
        roundChangeManager.appendRoundChangeMessage(signedPayload);
    final MessageAge messageAge = determineAgeOfPayload(signedPayload.getPayload());
    if (messageAge == PRIOR_ROUND) {
      LOG.info("Received RoundChange Payload for a prior round.");
      return;
    }
    final ConsensusRoundIdentifier targetRound = signedPayload.getPayload().getRoundIdentifier();
    LOG.info("Received a RoundChange Payload for {}", targetRound.toString());

    if (result.isPresent()) {
      if (messageAge == FUTURE_ROUND) {
        startNewRound(targetRound.getRoundNumber());
      }

      if (finalState.isLocalNodeProposerForRound(targetRound)) {
        currentRound.startRoundWith(result.get(), clock.millis() / 1000);
      }
    }
  }

  private void startNewRound(final int roundNumber) {
    LOG.info("Starting new round {}", roundNumber);
    if (futureRoundStateBuffer.containsKey(roundNumber)) {
      currentRound =
          roundFactory.createNewRoundWithState(
              parentHeader, futureRoundStateBuffer.get(roundNumber));
      futureRoundStateBuffer.keySet().removeIf(k -> k <= roundNumber);
    } else {
      currentRound = roundFactory.createNewRound(parentHeader, roundNumber);
    }
    // discard roundChange messages from the current and previous rounds
    roundChangeManager.discardRoundsPriorTo(currentRound.getRoundIdentifier());
    roundTimer.startTimer(currentRound.getRoundIdentifier());
  }

  public void handleNewRoundPayload(final SignedData<NewRoundPayload> signedPayload) {
    final NewRoundPayload payload = signedPayload.getPayload();
    final MessageAge messageAge = determineAgeOfPayload(payload);

    if (messageAge == PRIOR_ROUND) {
      LOG.info("Received NewRound Payload for a prior round.");
      return;
    }
    LOG.info("Received NewRound Payload for {}", payload.getRoundIdentifier().toString());

    if (newRoundMessageValidator.validateNewRoundMessage(signedPayload)) {
      if (messageAge == FUTURE_ROUND) {
        startNewRound(payload.getRoundIdentifier().getRoundNumber());
      }
      currentRound.handleProposalMessage(payload.getProposalPayload());
    }
  }

  public long getChainHeight() {
    return currentRound.getRoundIdentifier().getSequenceNumber();
  }

  private MessageAge determineAgeOfPayload(final Payload payload) {
    final int messageRoundNumber = payload.getRoundIdentifier().getRoundNumber();
    final int currentRoundNumber = currentRound.getRoundIdentifier().getRoundNumber();
    if (messageRoundNumber > currentRoundNumber) {
      return FUTURE_ROUND;
    } else if (messageRoundNumber == currentRoundNumber) {
      return CURRENT_ROUND;
    }
    return PRIOR_ROUND;
  }
}
