package keth.tools.crypto;

import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.asn1.x9.X9IntegerConverter;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.math.ec.ECAlgorithms;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.custom.sec.SecP256K1Curve;
import org.gradle.internal.impldep.com.google.common.base.Objects;
import tech.pegasys.pantheon.crypto.SECP256K1;
import tech.pegasys.pantheon.crypto.SecureRandomProvider;
import tech.pegasys.pantheon.util.bytes.Bytes32;
import tech.pegasys.pantheon.util.bytes.BytesValue;
import tech.pegasys.pantheon.util.bytes.MutableBytesValue;
import tech.pegasys.pantheon.util.uint.UInt256Bytes;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPairGenerator;
import java.security.Security;
import java.security.spec.ECGenParameterSpec;
import java.util.Arrays;

import static tech.pegasys.pantheon.util.bytes.BytesValues.asUnsignedBigInteger;

/**
 * motivation is this question
 *
 *  if we perform internal account management operations will we need pantheon crypto artifacts ?
 *
 *  many of the pantheon crypto artifacts are optimised for ethereum eg only sign Byte32, private sig constructors  ect
 *
 * temp work around
 *
 */
public class CryptoUtils {

    private static final String ALGORITHM = "ECDSA";
    private static final String CURVE_NAME = "secp256k1";
    private static final String PROVIDER = "BC";

    public static final ECDomainParameters CURVE;
    public static final BigInteger HALF_CURVE_ORDER;

    private static final KeyPairGenerator KEY_PAIR_GENERATOR;
    private static final BigInteger CURVE_ORDER;

    static {
        Security.addProvider(new BouncyCastleProvider());

        final X9ECParameters params = SECNamedCurves.getByName(CURVE_NAME);
        CURVE = new ECDomainParameters(params.getCurve(), params.getG(), params.getN(), params.getH());
        CURVE_ORDER = CURVE.getN();
        HALF_CURVE_ORDER = CURVE_ORDER.shiftRight(1);
        try {
            KEY_PAIR_GENERATOR = KeyPairGenerator.getInstance(ALGORITHM, PROVIDER);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
        final ECGenParameterSpec ecGenParameterSpec = new ECGenParameterSpec(CURVE_NAME);
        try {
            KEY_PAIR_GENERATOR.initialize(ecGenParameterSpec, SecureRandomProvider.createSecureRandom());
        } catch (final InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        }
    }



    public static AccountSignature create(final BigInteger r, final BigInteger s, final byte recId) {

        if (recId != 0 && recId != 1) {
            throw new IllegalArgumentException(
                    "Invalid 'recId' value, should be 0 or 1 but got " + recId);
        }
        return new  AccountSignature(r, s, recId);
    }




    public static boolean verify(
            final BytesValue data, final AccountSignature signature, final SECP256K1.PublicKey pub) {
        final ECDSASigner signer = new ECDSASigner();
        final BytesValue toDecode = BytesValue.wrap(BytesValue.of((byte) 4), pub.getEncodedBytes());
        final ECPublicKeyParameters params =
                new ECPublicKeyParameters(CURVE.getCurve().decodePoint(toDecode.extractArray()), CURVE);
        signer.init(false, params);
        try {
            return signer.verifySignature(data.extractArray(), signature.r, signature.s);
        } catch (final NullPointerException e) {
            // Bouncy Castle contains a bug that can cause NPEs given specially crafted signatures. Those
            // signatures
            // are inherently invalid/attack sigs so we just fail them here rather than crash the thread.
            return false;
        }
    }



    public static class AccountSignature {
        public static final int BYTES_REQUIRED = 65;
        /**
         * The recovery id to reconstruct the public key used to create the signature.
         *
         * <p>The recId is an index from 0 to 3 which indicates which of the 4 possible keys is the
         * correct one. Because the key recovery operation yields multiple potential keys, the correct
         * key must either be stored alongside the signature, or you must be willing to try each recId
         * in turn until you find one that outputs the key you are expecting.
         */
        private final byte recId;

        private final BigInteger r;
        private final BigInteger s;

        public AccountSignature(final BigInteger r, final BigInteger s, int recId) {
            this.r = r;
            this.s = s;
            this.recId = (byte) recId;
        }

        public static AccountSignature create(final BigInteger r, final BigInteger s, final byte recId) {

            checkInBounds("r", r);
            checkInBounds("s", s);
            if (recId != 0 && recId != 1) {
                throw new IllegalArgumentException(
                        "Invalid 'recId' value, should be 0 or 1 but got " + recId);
            }
            return new AccountSignature(r, s, recId);
        }

        private static void checkInBounds(final String name, final BigInteger i) {
            if (i.compareTo(BigInteger.ONE) < 0) {
                throw new IllegalArgumentException(
                        String.format("Invalid '%s' value, should be >= 1 but got %s", name, i));
            }

            if (i.compareTo(CURVE_ORDER) >= 0) {
                throw new IllegalArgumentException(
                        String.format("Invalid '%s' value, should be < %s but got %s", CURVE_ORDER, name, i));
            }
        }

        public static AccountSignature decode(final BytesValue bytes) {


            final BigInteger r = asUnsignedBigInteger(bytes.slice(0, 32));
            final BigInteger s = asUnsignedBigInteger(bytes.slice(32, 32));
            final byte recId = bytes.get(64);
            return AccountSignature.create(r, s, recId);
        }

        public BytesValue encodedBytes() {
            final MutableBytesValue bytes = MutableBytesValue.create(BYTES_REQUIRED);
            UInt256Bytes.of(r).copyTo(bytes, 0);
            UInt256Bytes.of(s).copyTo(bytes, 32);
            bytes.set(64, recId);
            return bytes;
        }

        @Override
        public boolean equals(final Object other) {
            if (!(other instanceof SECP256K1.Signature)) {
                return false;
            }

            final AccountSignature that = (AccountSignature) other;
            return this.r.equals(that.r) && this.s.equals(that.s) && this.recId == that.recId;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(r, s, recId);
        }

        public byte getRecId() {
            return recId;
        }

        public BigInteger getR() {
            return r;
        }

        public BigInteger getS() {
            return s;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append("SECP256K1.Signature").append("{");
            sb.append("r=").append(r).append(", ");
            sb.append("s=").append(s).append(", ");
            sb.append("recId=").append(recId);
            return sb.append("}").toString();
        }
    }



    public static BigInteger recoverFromSignature(
            final int recId, final BigInteger r, final BigInteger s, final BytesValue dataHash) {
        assert (recId >= 0);
        assert (r.signum() >= 0);
        assert (s.signum() >= 0);
        assert (dataHash != null);

        // 1.0 For j from 0 to h (h == recId here and the loop is outside this function)
        // 1.1 Let x = r + jn
        final BigInteger n = CURVE.getN(); // Curve order.
        final BigInteger i = BigInteger.valueOf((long) recId / 2);
        final BigInteger x = r.add(i.multiply(n));
        // 1.2. Convert the integer x to an octet string X of length mlen using the conversion
        // routine specified in Section 2.3.7, where mlen = ⌈(log2 p)/8⌉ or mlen = ⌈m/8⌉.
        // 1.3. Convert the octet string (16 set binary digits)||X to an elliptic curve point R
        // using the conversion routine specified in Section 2.3.4. If this conversion
        // routine outputs "invalid", then do another iteration of Step 1.
        //
        // More concisely, what these points mean is to use X as a compressed public key.
        final BigInteger prime = SecP256K1Curve.q;
        if (x.compareTo(prime) >= 0) {
            // Cannot have point co-ordinates larger than this as everything takes place modulo Q.
            return null;
        }
        // Compressed keys require you to know an extra bit of data about the y-coord as there are
        // two possibilities. So it's encoded in the recId.
        final ECPoint R = decompressKey(x, (recId & 1) == 1);
        // 1.4. If nR != point at infinity, then do another iteration of Step 1 (callers
        // responsibility).
        if (!R.multiply(n).isInfinity()) {
            return null;
        }
        // 1.5. Compute e from M using Steps 2 and 3 of ECDSA signature verification.
        final BigInteger e = asUnsignedBigInteger(dataHash);
        // 1.6. For k from 1 to 2 do the following. (loop is outside this function via
        // iterating recId)
        // 1.6.1. Compute a candidate public key as:
        // Q = mi(r) * (sR - eG)
        //
        // Where mi(x) is the modular multiplicative inverse. We transform this into the following:
        // Q = (mi(r) * s ** R) + (mi(r) * -e ** G)
        // Where -e is the modular additive inverse of e, that is z such that z + e = 0 (mod n).
        // In the above equation ** is point multiplication and + is point addition (the EC group
        // operator).
        //
        // We can find the additive inverse by subtracting e from zero then taking the mod. For
        // example the additive inverse of 3 modulo 11 is 8 because 3 + 8 mod 11 = 0, and
        // -3 mod 11 = 8.
        final BigInteger eInv = BigInteger.ZERO.subtract(e).mod(n);
        final BigInteger rInv = r.modInverse(n);
        final BigInteger srInv = rInv.multiply(s).mod(n);
        final BigInteger eInvrInv = rInv.multiply(eInv).mod(n);
        final ECPoint q = ECAlgorithms.sumOfTwoMultiplies(CURVE.getG(), eInvrInv, R, srInv);

        if (q.isInfinity()) {
            return null;
        }

        final byte[] qBytes = q.getEncoded(false);
        // We remove the prefix
        return new BigInteger(1, Arrays.copyOfRange(qBytes, 1, qBytes.length));
    }



    /** Decompress a compressed public key (x co-ord and low-bit of y-coord). */
    private static ECPoint decompressKey(final BigInteger xBN, final boolean yBit) {
        final X9IntegerConverter x9 = new X9IntegerConverter();
        final byte[] compEnc = x9.integerToBytes(xBN, 1 + x9.getByteLength(CURVE.getCurve()));
        compEnc[0] = (byte) (yBit ? 0x03 : 0x02);

        return CURVE.getCurve().decodePoint(compEnc);
    }

}
