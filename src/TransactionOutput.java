import java.security.PublicKey;

public class TransactionOutput {
    private String id;
    private PublicKey recipient; // also the new owner of these coins
    private float value;
    private String parentTransactionId; // the id of the transaction ths output was created in

    public String getId() {
        return id;
    }

    public PublicKey getRecipient() {
        return recipient;
    }

    public float getValue() {
        return value;
    }


    public TransactionOutput(PublicKey recipient, float value, String parentTransactionId){
        this.recipient = recipient;
        this.value = value;
        this.parentTransactionId = parentTransactionId;
        this.id = StringUtil.applySha256(StringUtil.getStringFromKey(recipient) + Float.toString(value) + parentTransactionId);
    }

    // check if coin belongs to you
    public boolean isMine(PublicKey publicKey){
        return (publicKey == recipient);
    }
}
