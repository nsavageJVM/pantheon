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
package tech.pegasys.pantheon.tests.acceptance.dsl.transaction.net;

import static org.assertj.core.api.Assertions.assertThat;

import tech.pegasys.pantheon.tests.acceptance.dsl.transaction.PantheonWeb3j;
import tech.pegasys.pantheon.tests.acceptance.dsl.transaction.Transaction;

import java.io.IOException;
import java.math.BigInteger;

import org.web3j.protocol.core.methods.response.NetPeerCount;

public class NetPeerCountTransaction implements Transaction<BigInteger> {

  NetPeerCountTransaction() {}

  @Override
  public BigInteger execute(final PantheonWeb3j node) {
    try {
      final NetPeerCount result = node.netPeerCount().send();
      assertThat(result).isNotNull();
      assertThat(result.hasError()).isFalse();
      return result.getQuantity();
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }
}
