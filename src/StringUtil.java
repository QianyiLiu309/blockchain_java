import java.lang.reflect.Array;
import java.security.*;
import java.util.ArrayList;
import java.util.Base64;

public class StringUtil {
    public static String applySha256(String input){
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes("UTF-8"));
            StringBuffer hexString = new StringBuffer(); // contain has as hexidecimal
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();

        }
        catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public static String getStringFromKey(Key key){
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    // apply ECDSA signature and returns the result (as bytes)
    public static byte[] applyECDSASig(PrivateKey privateKey, String input){
        Signature dsa;
        byte[] output = new byte[0];
        try{
            dsa = Signature.getInstance("ECDSA", "BC");
            dsa.initSign(privateKey);
            byte[] strByte = input.getBytes();
            dsa.update(strByte);

            output = dsa.sign();
            return output;
        }
        catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    // verify a String signature
    public static boolean verityECDSASig(PublicKey publicKey, String data, byte[] signature){
        try{
            // similar to creating signature, but use publicKey instead, then verity the two signatures by comparing in some magical ways
            Signature ecdsaVerify = Signature.getInstance("ECDSA", "BC");
            ecdsaVerify.initVerify(publicKey);
            ecdsaVerify.update(data.getBytes());
            return ecdsaVerify.verify(signature);

        }
        catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    // tacks in array of transactions and returns a merkle root
    public static String getMerkleRoot(ArrayList<Transaction> transactions){
        int count = transactions.size();
        ArrayList<String> previousTreeLayer = new ArrayList<>();
        for(Transaction transaction: transactions){
            previousTreeLayer.add(transaction.transactionId);
        }
        ArrayList<String> treeLayer = previousTreeLayer;
        while(count > 1){
            treeLayer = new ArrayList<>();
            for(int i = 1; i < previousTreeLayer.size(); i ++){
                treeLayer.add(applySha256(previousTreeLayer.get(i-1) + previousTreeLayer.get(i)));
            }
            count = treeLayer.size();
            previousTreeLayer = treeLayer;
        }
        String merkleRoot = (treeLayer.size() == 1) ? treeLayer.get(0) : "";
        return merkleRoot;
    }
}
