//购物车服务层
app.service('cartService',function($http){
        //购物车列表
        this.findCartList=function(){
            return $http.get('cart/findCartList.do');
        }

        //添加商品到购物车
        this.addGoodsToCartList = function (itemId,num) {
            return $http.get('cart/addGoodsToCartList.do?itemId='+itemId+"&num="+num);
        }


    //求合计
    this.sum=function(cartList){
        //定义一个合计实体
        totalValue = {totalNum:0,totalPrice:0.00};

        for (var i = 0 ;i<cartList.length;i++){
            var cart = cartList[i];
            for (var j = 0; j < cart.orderItemList.length;j++) {
                var orderItem = cart.orderItemList[j];
                totalValue.totalNum = totalValue.totalNum + orderItem.num;
                totalValue.totalPrice = totalValue.totalPrice + orderItem.totalFee;
            }
        }
        return totalValue;
    }

    //用户的收货地址
    this.findAddressList=function(){
        return $http.get('address/findListByLoginUser.do');
    }


    //保存订单
    this.submitOrder=function(order){
        return $http.post('order/add.do',order);
    }






});
