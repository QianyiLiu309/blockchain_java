import java.lang.reflect.Array;
import java.util.ArrayList;
import com.google.gson.GsonBuilder;

import javax.swing.text.StyledEditorKit;

public class BlockChain {
    public static ArrayList<Block> blockchain = new ArrayList<>();
    public static int difficulty = 6;

    public static Boolean isChainValid(){
        Block currentBlock;
        Block previousBlock;
        String hashTarget = new String(new char[difficulty]).replace('\0', '0');

        //loop through blockchain to check hashes
        for(int i = 1; i < blockchain.size(); i ++){
            currentBlock = blockchain.get(i);
            previousBlock = blockchain.get(i - 1);

            // compare the registered hash and calculated hash
            if(!currentBlock.hash.equals(currentBlock.calculateHash())){
                System.out.println("Current Hashes not equal");
                return false;
            }

            // compare previous hash and registered previous hash
            if(!currentBlock.previousHash.equals(previousBlock.hash)){
                System.out.println("Previous Hashes not equal");
                return false;
            }

            //check if hash is solved
            if(!currentBlock.hash.substring(0, difficulty).equals(hashTarget)){
                System.out.println("This block hasn't been mined");
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        Block genesisBlock = new Block("Hi im the first block", "0");
        blockchain.add(genesisBlock);
        System.out.println("Trying to Mine bloc 1... ");
        blockchain.get(0).mineBlock(difficulty);

        Block secondBlock = new Block("Yo im the second block", genesisBlock.hash);
        blockchain.add(secondBlock);
        System.out.println("Trying to Mine bloc 2... ");
        blockchain.get(1).mineBlock(difficulty);

        Block thirdBlock = new Block("Hey im the third block", secondBlock.hash);
        blockchain.add(thirdBlock);
        System.out.println("Trying to Mine bloc 3... ");
        blockchain.get(2).mineBlock(difficulty);

        System.out.println("\nBlockchain is Valid: " + isChainValid());

        String blockchainJson = new GsonBuilder().setPrettyPrinting().create().toJson(blockchain);
        System.out.println("\nThe block chain: ");
        System.out.println(blockchainJson);
    }

}
