public class TransactionInput {
    private String transactionOutputId; // reference to transactionoutputs -> transactionId
    private TransactionOutput UTXO; // contains the unspent transaction output

    public String getTransactionOutputId(){
        return transactionOutputId;
    }

    public TransactionOutput getUTXO(){
        return UTXO;
    }

    public void setUTXO(TransactionOutput temp){
        this.UTXO = temp;
    }

    public TransactionInput(String transactionOutputId){
        this.transactionOutputId = transactionOutputId;
    }
}
