import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;

public class NoobChain {
    private static ArrayList<Block> blockchain = new ArrayList<>();
    private static HashMap<String, TransactionOutput> UTXOs = new HashMap<>(); // list of all unspent transactions

    private static int difficulty = 5;
    private static float minimumTransaction = 0.1f;
    private static Wallet walletA;
    private static Wallet walletB;
    private static Transaction genesisTransaction;

    public static HashMap<String, TransactionOutput> getUTXOs(){
        return UTXOs;
    }

    public static float getMinimumTransaction(){
        return minimumTransaction;
    }

    public static Boolean isChainValid(){
        Block currentBlock;
        Block previousBlock;
        String hashTarget = new String(new char[difficulty]).replace('\0', '0');
        HashMap<String, TransactionOutput> tempUTXOs = new HashMap<>(); // a temporary working list of unspent transactions at a given block state
        tempUTXOs.put(genesisTransaction.getOutputs().get(0).getId(), genesisTransaction.getOutputs().get(0));


        //loop through blockchain to check hashes
        for(int i = 1; i < blockchain.size(); i ++){
            currentBlock = blockchain.get(i);
            previousBlock = blockchain.get(i - 1);

            // compare the registered hash and calculated hash
            if(!currentBlock.getHash().equals(currentBlock.calculateHash())){
                System.out.println("Current Hashes not equal");
                return false;
            }

            // compare previous hash and registered previous hash
            if(!currentBlock.getPreviousHash().equals(previousBlock.getHash())){
                System.out.println("Previous Hashes not equal");
                return false;
            }

            //check if hash is solved
            if(!currentBlock.getHash().substring(0, difficulty).equals(hashTarget)){
                System.out.println("This block hasn't been mined");
                return false;
            }

            // loop through blockchains transactions:
            TransactionOutput tempOutput;
            for(int t = 0; t < currentBlock.getTransactions().size(); t ++){
                Transaction currentTransaction = currentBlock.getTransactions().get(t);

                if(!currentTransaction.verifySignature()){
                    System.out.println("#Signature on Transaction " + t + " is invalid");
                    return false;
                }
                if(currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()){
                    System.out.println("#Inputs are not equal to outputs on Transaction " + t);
                    return false;
                }
                for(TransactionInput input: currentTransaction.getInputs()){
                    tempOutput = tempUTXOs.get(input.getTransactionOutputId());

                    if(tempOutput == null){
                        System.out.println("#Referenced input on Transaction " + t + " is missing");
                        return false;
                    }

                    if(input.getUTXO().getValue() != tempOutput.getValue()){
                        System.out.println("#Referenced input Transaction " + t + " value is Invalid");
                        return false;
                    }

                    tempUTXOs.remove(input.getTransactionOutputId());
                }

                for(TransactionOutput output: currentTransaction.getOutputs()){
                    tempUTXOs.put(output.getId(), output);
                }

                if(currentTransaction.getOutputs().get(0).getRecipient() != currentTransaction.getRecipient()){
                    System.out.println("#Transaction " + t + " output recipient is not who it should be");
                    return false;
                }
                if(currentTransaction.getOutputs().get(1).getRecipient() != currentTransaction.getSender()){
                    System.out.println("#Transaction " + t + " output 'change' is not the sender.");
                    return false;
                }
            }
        }
        System.out.println("Blockchain is valid");
        return true;
    }

    public static void main(String[] args) {
        // add our blocks to the blockchain arraylist

        // setup Bouncey castle as a Security Provider
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        // Create the new wallets
        walletA = new Wallet();
        walletB = new Wallet();
        Wallet coinbase = new Wallet();

        // create genesis transaction, which sends 100 Noobcoin to walletA:
        genesisTransaction = new Transaction(coinbase.getPublicKey(), walletA.getPublicKey(), 100f, null);
        genesisTransaction.generateSignature(coinbase.getPrivateKey()); // manually sign the genesis transaction
        genesisTransaction.setTransactionId("0");
        genesisTransaction.getOutputs().add(new TransactionOutput(genesisTransaction.getRecipient(), genesisTransaction.getValue(), genesisTransaction.getTransactionId()));
        UTXOs.put(genesisTransaction.getOutputs().get(0).getId(), genesisTransaction.getOutputs().get(0));

        System.out.println("Creating and Mining Genesis block... ");
        Block genesis = new Block("0");
        genesis.addTransaction(genesisTransaction);
        addBlock(genesis);

        // testing
        Block block1 = new Block(genesis.getHash());
        System.out.println("\nWalletA's balance is: " + walletA.getBalance());
        System.out.println("\nWalletA is attempting to send funds (40) to WalletB... ");
        block1.addTransaction(walletA.sendFunds(walletB.getPublicKey(), 40f));
        addBlock(block1);
        System.out.println("\nWalletA's balance is: " + walletA.getBalance());
        System.out.println("\nWalletB's balance is: " + walletB.getBalance());

        Block block2 = new Block(block1.getHash());
        System.out.println("\nWalletA is attempting to send more funds (1000) than it has... ");
        block2.addTransaction(walletA.sendFunds(walletB.getPublicKey(), 1000f));
        addBlock(block2);
        System.out.println("\nWalletA's balance is: " + walletA.getBalance());
        System.out.println("\nWalletB's balance is: " + walletB.getBalance());

        Block block3 = new Block(block2.getHash());
        System.out.println("\nWalletB is attempting to send funds (20) to walletA... ");
        block3.addTransaction(walletB.sendFunds(walletA.getPublicKey(), 20f));
        System.out.println("\nWalletA is attempting to send funds (5) to walletB...");
        block3.addTransaction(walletA.sendFunds(walletB.getPublicKey(), 5f));
        addBlock(block3);
        System.out.println("\nWalletA's balance is: " + walletA.getBalance());
        System.out.println("\nWalletB's balance is: " + walletB.getBalance());

        isChainValid();

        walletA.printTransactionHistory();
        walletB.printTransactionHistory();


        /*// Test public and private keys
        System.out.println("Private and public keys: ");
        System.out.println(StringUtil.getStringFromKey(walletA.privateKey));
        System.out.println(StringUtil.getStringFromKey(walletA.publicKey));

        // create a test transaction from WalletA to walletB
        Transaction transaction = new Transaction(walletA.publicKey, walletB.publicKey, 5, null);
        transaction.generateSignature(walletA.privateKey);

        // verify the signature works and verify it from the public key
        System.out.println("Is signature verified");
        System.out.println(transaction.verifySignature());*/

        /*
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
        System.out.println(blockchainJson);*/
    }

    public static void addBlock(Block newBlock){
        newBlock.mineBlock(difficulty);
        blockchain.add(newBlock);
    }

}
