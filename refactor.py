import sys

filepath = r'd:\Petshop2\PetShop\src\main\java\controller\shop\CheckoutServlet.java'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

start_marker = r'try (Connection conn = DBContext.getConnection()) {'
end_marker = r'} finally {' + '\n' + r'                    conn.setAutoCommit(true);' + '\n' + r'                }'

start_idx = content.find(start_marker)
end_idx = content.find(end_marker, start_idx)
if end_idx != -1:
    end_idx += len(end_marker)

if start_idx != -1 and end_idx != -1:
    new_logic = """String paymentMethodKey = resolvePaymentMethodKey(request);

            services.CheckoutResult checkoutResult = checkoutService.processCheckout(
                user, cart, fullAddress, note, couponState, paymentMethodKey
            );

            if (!checkoutResult.isSuccess()) {
                result.put("success", false);
                result.put("message", checkoutResult.getMessage());
                write(response, result);
                return;
            }

            completedPaymentMethod = checkoutResult.getPaymentMethodDb();
            completedPaymentTransaction = checkoutResult.getPaymentTransaction();
            completedOrderId = checkoutResult.getOrderId();"""
            
    content = content[:start_idx] + new_logic + content[end_idx:]
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)
    print('Refactoring CheckoutServlet successful.')
else:
    print('Markers not found. start:', start_idx, 'end:', end_idx)
