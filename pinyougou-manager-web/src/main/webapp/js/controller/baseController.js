//存放通用的controller
app.controller('baseController',function ($scope) {
    //重新加载列表 数据
    $scope.reloadList=function(){
        //切换页码
        $scope.search($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
    }

    //分页控件配置currentPage 当前页码,totalItems总记录数.itemsPerPage每页记录,perPageOptions分页选项
    $scope.paginationConf = {
        currentPage: 1,
        totalItems: 10,
        itemsPerPage: 10,
        perPageOptions: [10, 20, 30, 40, 50],
        onChange: function(){
            //alert("onChange");
            $scope.reloadList();//重新加载
        }
    };

    $scope.selectIds = []; //选中id集合
    //向id集合中加减id
    $scope.updateSelection = function ($event, id) {

        var put = $event.target;        //相当于获取input标签 ,  $event相当于源
        if(put.checked){ //判断是否选中
            $scope.selectIds.push(id);
        } else {
            //获取指定id在数组中的下标
            var idx = $scope.selectIds.indexOf(id);
            //从数组移除指定id
            $scope.selectIds.splice(idx,1);

        }
    }



    //提取json字符串数据中某个属性，返回拼接字符串 逗号分隔
    $scope.jsonToString=function(jsonString,key){
        var json=JSON.parse(jsonString);//将json字符串转换为json对象
        var value="";
        for(var i=0;i<json.length;i++){
            if(i>0){
                value+=","
            }
            value+=json[i][key];
        }
        return value;
    }

})