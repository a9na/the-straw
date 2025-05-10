package com.ryz.coffee.admin.fragment.order.tab.tab2

data class OrderPackedModel(
    var email: String? = null,
    var count: String? = null,
    var image: String? = null,
    var orderId: String? = null,
    var title: String? = null,
    var totalPrice: String? = null,
    var address: String? = null,
    var paymentMethod: String? = null,
    var statusOrder: String? = null,
    var subTotalDelivery: String? = null,
    var subTotalProduct: String? = null,
    var totalPayment: String? = null,
    var totalChildren: String? = null
)