//创建控制器
app.controller('brandController',function ($scope,$controller,brandService) {
    //继承baseController
    $controller('baseController',{$scope:$scope});


    $scope.findAll=function () {
        brandService.findAll().success(
            function (response) {
                $scope.list=response;
            });
    }

    //分页
    $scope.findPage=function(page,rows){
        brandService.findPage(page,rows).success(
            function(response){
                $scope.list=response.rows;
                $scope.paginationConf.totalItems=response.total;//更新总记录数
            }
        );
    }

    //保存
    $scope.save=function () {
        var serviceObject;  //服务层对象

        if($scope.entity.id != null){  //如果存在id则更新方法
            serviceObject = brandService.update($scope.entity);
        } else {
            serviceObject = brandService.add($scope.entity);
        }

        serviceObject.success(
            function (response) {
                // alert(response.success);
                if(response.success){
                    //重新查询
                    $scope.reloadList();
                } else {
                    alert(response.message);
                }
            }
        )
    }

    //查询
    $scope.findById=function (id) {
        //alert("查询");
        brandService.findById(id).success(
            function (response) {
                $scope.entity=response;
            }
        )
    }




    //批量删除
    $scope.dele = function () {
        brandService.dele($scope.selectIds).success(
            function (response) {
                if(response.success){
                    //重新查询
                    $scope.reloadList();
                } else {
                    alert(response.message);
                }
            }
        )
    }


    $scope.searchEntity = {};
    //查询
    $scope.search = function (page,rows) {
        brandService.search(page,rows,$scope.searchEntity).success(
            function(response) {
                $scope.list=response.rows;
                $scope.paginationConf.totalItems=response.total;//更新总记录数
            }
        );

    }
});