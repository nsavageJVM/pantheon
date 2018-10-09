package net.consensys.pantheon.ethereum.eth.manager.exceptions;

public class IncompleteResultsException extends EthTaskException {

  public IncompleteResultsException() {
    super(FailureReason.INCOMPLETE_RESULTS);
  }
}