package btdex.sellContract.dispute;

import bt.BT;
import btdex.sc.SellContract;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

//fails in "fresh" IDE IntelliJ, later passes
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestMakerDisputeWithdraw {
    private static InitSC sc;
    private static long state;
    private static long amountToMaker = 10000;
    private static long amountToTaker = 0;

    @Test
    @Order(1)
    public void initSC() {
        sc = new InitSC();

        long state = SellContract.STATE_WAITING_PAYMT;
        long state_chain = sc.getContractFieldValue("state");

        if(state_chain == -1){
            System.out.println("Contract not found");
            return;
        } else if(state_chain == -2){
            System.out.println("Field not found");
            return;
        }
        assertEquals(state, state_chain);

    }

    @Test
    @Order(2)
    public void testMakerDispute() {
        sc.dispute(sc.getMaker(), amountToMaker, amountToTaker);
        BT.forgeBlock();
        BT.forgeBlock();

        state = SellContract.STATE_CREATOR_DISPUTE;
        assertEquals(state, state&sc.getContractFieldValue("state"));
        assertEquals(amountToMaker, sc.getContractFieldValue("disputeCreatorAmountToCreator"));
        assertEquals(amountToTaker, sc.getContractFieldValue("disputeCreatorAmountToTaker"));
        assertEquals(0, sc.getContractFieldValue("disputeTakerAmountToTaker"));
        assertEquals(0, sc.getContractFieldValue("disputeTakerAmountToCreator"));
    }

    @Test
    @Order(3)
    public void testWithdraw() {
        long feeContractBalance = sc.getFeeContractBalance();
        long makerBalance = sc.getMakerBalance();
        long takerBalance = sc.getTakerBalance();
        sc.withdraw();
        BT.forgeBlock();
        BT.forgeBlock();

        state = SellContract.STATE_CREATOR_DISPUTE;
        assertEquals(state, state&sc.getContractFieldValue("state"));
        assertTrue(sc.getSCbalance() > 0);
        assertTrue(takerBalance == sc.getTakerBalance());
        assertTrue(sc.getMakerBalance() < makerBalance);
        assertTrue(sc.getFeeContractBalance() == feeContractBalance, "FeeContract got transaction");
    }
}
