app.controller('searchController',function($scope,searchService){

	$scope.searchMap={'keywords':'','category':'','brand':'','spec':{}};
	
	//搜索
	$scope.search=function(){
		searchService.search($scope.searchMap).success(
			function(response){
				$scope.resultMap=response;				
			}
		);		
	}

	$scope.addSearchItem=function (key,value) {

		if(key == 'category' || key == 'brand'){
			$scope.searchMap[key]=value;
		}else {
			$scope.searchMap.spec[key]=value;
		}
		$scope.search();
	}

	$scope.deleSearchItem=function (key) {
		if(key == 'category' || key == 'brand'){
			$scope.searchMap[key]='';
		}else {
			delete $scope.searchMap.spec[key]
		}
		$scope.search();
	}
	
	
});