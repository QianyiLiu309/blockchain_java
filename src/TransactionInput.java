public class TransactionInput {
    public String transactionOutputId; // reference to transactionoutputs -> transactionId
    public TransactionOutput UTXO; // contains the unspent transaction output

    public TransactionInput(String transactionOutputId){
        this.transactionOutputId = transactionOutputId;
    }
}
