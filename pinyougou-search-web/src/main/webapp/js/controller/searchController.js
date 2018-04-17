app.controller('searchController',function($scope,$location,searchService){
    //搜索
    $scope.search=function(){
        //alert("search");
        //将页码转成int数据
        $scope.searchMap.pageNo= parseInt($scope.searchMap.pageNo) ;

        searchService.search( $scope.searchMap ).success(
            function(response){
                $scope.resultMap=response;//搜索返回的结果
                buildPageLabel();//调用
            }
        );
        //buildPageLabel();//调用

    }

    //搜索对象
    $scope.searchMap={'keywords':'','category':'','brand':'','spec':{},'price':'','pageNo':1,'pageSize':40,'sortField':'','sort':'' };

    //添加搜索项
    $scope.addSearchItem=function (key,value) {
        //alert("key");
        //如果点击的是分类或者是品牌
        if(key == 'category' || key == 'brand' || key == 'price'){
            $scope.searchMap[key] = value;
        }else {
            $scope.searchMap.spec[key] = value;
        }
        $scope.search();//执行搜索
    }

    //移除复合搜索条件
    $scope.removeSearchItem=function(key){
        if(key=="category" ||  key=="brand" || key == 'price'){//如果是分类或品牌
            $scope.searchMap[key]="";
        }else{//否则是规格
            delete $scope.searchMap.spec[key];//移除此属性
        }
        $scope.search();//执行搜索
    }


    //构建分页标签(totalPages为总页数)
    buildPageLabel=function(){
        $scope.pageLabel=[]; //新增分页栏属性
        var maxPageNo = $scope.resultMap.totalPages;//得到最后页码
        var firstPage = 1 ; //开始页码
        var lastPage = maxPageNo; //截至页码

        var pageNo = $scope.searchMap.pageNo; //当前页码

        $scope.firstDot=true;//前面有点
        $scope.lastDot=true;//后边有点

        if(maxPageNo > 5){  //如果当前总页数大于5 显示部分页码
            if(pageNo <= 3){
                lastPage = 5;
                $scope.firstDot=false;//前面无点
            } else if (pageNo > maxPageNo - 2) {
                firstPage= maxPageNo-4;		 //后5页
                $scope.lastDot=false;//后边无点
            } else {
                firstPage = pageNo - 2;
                lastPage = pageNo + 2;
            }
        } else {
            $scope.firstDot=false;//前面无点
            $scope.lastDot=false;//后边无点
        }

        //循环产生页码标签
        for(var i=firstPage;i<=lastPage;i++){
            $scope.pageLabel.push(i);
        }
    }

    //根据页码查询
    $scope.queryByPage=function(pageNo){
        //alert(123);
        if(pageNo < 1 || pageNo > $scope.resultMap.totalPages){
            return;
        }
        $scope.searchMap.pageNo = pageNo;
        $scope.search();
    }

    //判断是否第一页
    $scope.isTopPage=function(){
        if($scope.searchMap.pageNo == 1){
            return true;
        }
        return false;
    }

    //判断是否时最后一页
    $scope.isEndPage=function(){
        if($scope.searchMap.pageNo == $scope.resultMap.totalPages) {
            return true;
        }
        return false;
    }

    //设置排序规则
    $scope.sortSearch=function(sortField,sort){
        $scope.searchMap.sortField=sortField;
        $scope.searchMap.sort=sort;
        $scope.search();
    }

    //判断关键字是不是品牌 如果包含则隐藏品牌
    $scope.keywordsIsBrand = function () {
        for (var i = 0 ; i < $scope.resultMap.brandList.length ; i++ ) {
            if($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text) >= 0){
                return true;
            }
        }
        return false;
    }

    $scope.loadkeywords = function () {
        //alert("加载关键字查询!");
        $scope.searchMap.keywords = $location.search()['keywords'];
        $scope.search();
    }
});