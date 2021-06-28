import com.sun.jdi.event.StepEvent;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;

public class Block {
    public String hash;
    public String previousHash;
    public String merkleRoot;
    public ArrayList<Transaction> transactions = new ArrayList<>();
    //private String data; // our data will be some simple message
    private long timeStamp;
    private int nonce;

    public Block(String previousHash){
        //this.data = data;
        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime();

        this.hash = calculateHash(); // initialization sequence ensures that we can safely call this function
    }

    public String calculateHash(){
        String calculatedHash = StringUtil.applySha256(previousHash +
                Long.toString(timeStamp) +
                Integer.toString(nonce) +
                merkleRoot);
        return calculatedHash;
    }

    public void mineBlock(int difficulty){
        merkleRoot = StringUtil.getMerkleRoot(transactions);
        hash = calculateHash();// we need to update our hash values when new transactions are inserted
        String target = new String(new char[difficulty]).replace('\0', '0');
        while(!hash.substring(0, difficulty).equals(target)){
            nonce ++;
            hash = calculateHash();
        }
        System.out.println("Block Mined!!!: " + hash);
    }

    // add transactions to this block
    public boolean addTransaction(Transaction transaction){
        if(transaction == null)
            return false;
        if(previousHash != "0"){
            if(transaction.processTransaction() != true){ // the transaction is processed when it's added to a block
                // user only create a transaction from their wallet, but it doesn't get processed immediately
                System.out.println("Transaction failed to process. Discarded.");
                return false;
            }
        }
        transactions.add(transaction);
        System.out.println("Transaction Successfully added to Block");
        return true;
    }
}
