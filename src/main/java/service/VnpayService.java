package service;
 
import controller.VnpayConfig;
import util.VnpayUtil;
 
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
 
public class VnpayService {
 
    /**
     * Build the VNPay payment redirect URL.
     *
     * KEY FIX: Amount is multiplied by 100 (VNPay requires integer, no decimal).
     *          Params MUST be sorted a→z before HMAC — VnpayUtil handles this.
     *
     * @param orderId   internal order ID (used in orderInfo for callback matching)
     * @param txnRef    unique transaction reference (store in DB before calling)
     * @param amount    payment amount in VND
     * @param ipAddr    client IP
     * @return full HTTPS URL to redirect to
     */
    public String buildPaymentUrl(int orderId, String txnRef, BigDecimal amount, String ipAddr) {
        long vnpAmount = amount.multiply(BigDecimal.valueOf(100)).longValue();
 
        Map<String, String> params = new LinkedHashMap<>();
        params.put("vnp_Version",    VnpayConfig.VNP_VERSION);
        params.put("vnp_Command",    VnpayConfig.VNP_COMMAND);
        params.put("vnp_TmnCode",    VnpayConfig.VNP_TMN_CODE);
        params.put("vnp_Amount",     String.valueOf(vnpAmount));
        params.put("vnp_CurrCode",   VnpayConfig.VNP_CURR_CODE);
        params.put("vnp_TxnRef",     txnRef);
        // OrderInfo used to extract orderId on return callback
        params.put("vnp_OrderInfo",  "Thanh toan don hang " + orderId);
        params.put("vnp_OrderType",  VnpayConfig.VNP_ORDER_TYPE);
        params.put("vnp_Locale",     VnpayConfig.VNP_LOCALE);
        params.put("vnp_ReturnUrl",  VnpayConfig.VNP_RETURN_URL);
        params.put("vnp_IpAddr",     ipAddr);
        params.put("vnp_CreateDate", VnpayUtil.now());
        params.put("vnp_ExpireDate", VnpayUtil.expireIn(15));
 
        return VnpayConfig.VNP_PAY_URL + "?" + VnpayUtil.buildQueryString(params, VnpayConfig.VNP_HASH_SECRET);
    }
}
 