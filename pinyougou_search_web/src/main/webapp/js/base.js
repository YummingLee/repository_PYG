// 定义模块:
var app = angular.module("pinyougou",[]);

app.filter("trustHtml",['$sce',function ($sce) {
    return function (data) {
        return $sce.trustAsHtml(data);
    }
}])