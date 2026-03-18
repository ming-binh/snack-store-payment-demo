package controller;

public class VnpayConfig {

    public static final String VNP_TMN_CODE    = "4YUP19I4";
    public static final String VNP_HASH_SECRET = "MDUIFDCRAKLNBPOFIAFNEKFRNMFBYEPX";
    public static final String VNP_PAY_URL     = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    public static final String VNP_RETURN_URL  = "http://localhost:8080/swd_payment_demo/vnpay_return";

    // Payment params
    public static final String VNP_VERSION    = "2.1.0";
    public static final String VNP_COMMAND    = "pay";
    public static final String VNP_ORDER_TYPE = "other";
    public static final String VNP_LOCALE     = "vn";
    public static final String VNP_CURR_CODE  = "VND";
}