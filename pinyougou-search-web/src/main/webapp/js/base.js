//不带分页模块
var app = angular.module('pinyougou',[]);   //添加分页模块


/*$sce服务写成过滤器*/
app.filter('trustHtml',['$sce',function($sce){
    return function(data){
        return $sce.trustAsHtml(data);
    }
}]);