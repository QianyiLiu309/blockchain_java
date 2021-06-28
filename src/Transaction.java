import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;

public class Transaction {
    private String transactionId; // this is also the hash of the transaction
    private PublicKey sender; // senders address
    private PublicKey recipient; // recipients address
    private float value;
    private byte[] signature; // to prevent anybody else from spending funds in our wallet
    private ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
    private ArrayList<TransactionOutput> outputs = new ArrayList<TransactionOutput>();
    private static int sequence = 0; // a rough count of how many transactions have been generated

    // a bunch of getter methods
    public String getTransactionId(){
        return transactionId;
    }

    public PublicKey getSender(){
        return sender;
    }

    public PublicKey getRecipient(){
        return recipient;
    }

    public float getValue(){
        return value;
    }

    public ArrayList<TransactionInput> getInputs(){
        return inputs;
    }

    public ArrayList<TransactionOutput> getOutputs(){
        return outputs;
    }


    public void setTransactionId(String s){
        transactionId = s;
    }

    // constructor
    public Transaction(PublicKey from, PublicKey to, float value, ArrayList<TransactionInput> inputs){
        this.sender = from;
        this.recipient = to;
        this.value = value;
        this.inputs = inputs;
        this.transactionId = calculateHash();
    }

    // calculates the transaction hash
    private String calculateHash(){
        sequence ++;  // increase the sequence to avoid 2 identical transactions having the same hash
        return StringUtil.applySha256(StringUtil.getStringFromKey(sender) +
                StringUtil.getStringFromKey(recipient) +
                Float.toString(value) + sequence);
    }

    // sign all the data we don't wish to be tampered with
    public void generateSignature(PrivateKey privateKey){
        String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(recipient) + Float.toString(value);
        signature = StringUtil.applyECDSASig(privateKey, data);
    }

    // verifies the data we signed hasn't been tampered with
    public boolean verifySignature(){
        String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(recipient) + Float.toString(value);
        return StringUtil.verityECDSASig(sender, data, signature);
    }

    // returns true if new transaction could be created
    public boolean processTransaction(){
        if(verifySignature() == false){
            System.out.println("#Transactoin signature failed to verify");
            return false;
        }

        // gather transaction inputs (make sure they are unspent):
        for(TransactionInput i: inputs){
            i.setUTXO(NoobChain.getUTXOs().get(i.getTransactionOutputId()));
        }

        // check if transaction is valid:
        if(getInputsValue() < NoobChain.getMinimumTransaction()){
            System.out.println("#Transaction inputs too small: " + getInputsValue());
            return false;
        }

        // generate transaction outputs:
        float leftOver = getInputsValue() - value;
        //transactionId = calculateHash();
        outputs.add(new TransactionOutput(this.recipient, value, transactionId)); // send value to recipient
        outputs.add(new TransactionOutput(this.sender, leftOver, transactionId)); // send the left over 'change' back to sender

        // add outputs to Unspent list
        for(TransactionOutput o: outputs){
            NoobChain.getUTXOs().put(o.getId(), o);
        }

        // remove transaction inputs from UTXO lists as spent:
        for(TransactionInput i: inputs){
            if(i.getUTXO() == null)
                continue;
            NoobChain.getUTXOs().remove(i.getUTXO().getId());
        }

        return true;
    }

    // returns sum of inputs(UTXOs) values
    public float getInputsValue(){
        float total = 0;
        for(TransactionInput i: inputs){
            if(i.getUTXO() == null)
                continue; // if Transaction can't be found skip it
            total += i.getUTXO().getValue();
        }
        return total;
    }

    // return sum of outputs
    public float getOutputsValue(){
        float total = 0;
        for(TransactionOutput o: outputs){
            total += o.getValue();
        }
        return total;
    }
}
