import java.lang.reflect.Array;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Wallet {
    public PrivateKey privateKey;
    public PublicKey publicKey;

    public HashMap<String, TransactionOutput> UTXOs = new HashMap<>(); // only UTXOs owned by this wallet
    public HashMap<String, Transaction> transactionHistory = new HashMap<>();

    public Wallet(){
        generateKeyPair();
    }

    public void generateKeyPair(){
        try{
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA", "BC");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");

            // initialize the key generator and generate a key pair
            keyGen.initialize(ecSpec, random);
            KeyPair keyPair = keyGen.generateKeyPair();

            // set the public and private keys from the keyPair
            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();
        }
        catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    // returns balance and stores the UTXO's owned by this wallet in this.UTXOs
    public float getBalance(){
        float total = 0;
        for(Map.Entry item: NoobChain.UTXOs.entrySet()){
            TransactionOutput UTXO = (TransactionOutput) item.getValue();
            if(UTXO.isMine(publicKey)){
                UTXOs.put(UTXO.id, UTXO);
                total += UTXO.value;
            }
        }
        return total;
    }

    // generates and returns a new transaction from this wallet
    public Transaction sendFunds(PublicKey recipient, float value){
        if(getBalance() < value){
            // gather balance and check funds
            System.out.println("# Not enough funds to send transaction. Transaction discarded.");
            return null;
        }
        // create array list of inputs
        ArrayList<TransactionInput> inputs = new ArrayList<>();
        float total = 0;
        for(Map.Entry item: UTXOs.entrySet()){
            TransactionOutput UTXO = (TransactionOutput) item.getValue();
            total += UTXO.value;
            inputs.add(new TransactionInput(UTXO.id));
            if(total > value)
                break;
        }

        Transaction newTransacion = new Transaction(publicKey, recipient, value, inputs);
        newTransacion.generateSignature(privateKey);

        for(TransactionInput input: inputs){
            UTXOs.remove(input.transactionOutputId);
        }
        transactionHistory.put(newTransacion.transactionId, newTransacion);
        return newTransacion;
    }

    public void printTransactionHistory(){
        System.out.println("Transaction history of " + StringUtil.getStringFromKey(publicKey));
        for(Map.Entry entry: transactionHistory.entrySet()){
            Transaction t = (Transaction) entry.getValue();
            System.out.println("TransactionId：" + t.transactionId + " Amount: " + t.value);
        }
    }
}
