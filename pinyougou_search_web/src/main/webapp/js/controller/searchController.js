app.controller('searchController',function($scope,$location,searchService){

	$scope.searchMap={'keywords':'','category':'','brand':'','spec':{},'price':'','pageNo':1,'pageSize':40,'sort':'','sortField':''};

	$scope.loadKeywords=function(){
		$scope.searchMap.keywords = $location.search()['keywords'];
		$scope.search();
	}

	buildPageLabel=function(){
		$scope.pageLabel=[];
		var maxPageNo = $scope.resultMap.totalPages;
		var firstPage = 1 ;
		var lastPage = maxPageNo;
		$scope.firstDot=true;
		$scope.lastDot=true;
		if(maxPageNo > 5){
			if($scope.searchMap.pageNo <= 3){
				lastPage = 5 ;
				$scope.firstDot=false;
			}else if ($scope.searchMap.pageNo >= lastPage -2){
				firstPage = maxPageNo -4 ;
				$scope.lastDot=false;
			}else {
				firstPage = $scope.searchMap.pageNo - 2 ;
				lastPage = $scope.searchMap.pageNo + 2 ;
			}

		}else {
		    $scope.firstDot=false;
		    $scope.lastDot=false;
        }

		for (var i = firstPage; i <= lastPage; i++) {
			$scope.pageLabel.push(i);

		}
	};

	$scope.keywordsIsBrand=function(){
        for (var i = 0; i < $scope.resultMap.brandList.length; i++) {

            if($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text )>=0){
                return true;
            }
        }
        return false;
    };

	$scope.sortSearch=function(sortField,sort){
	    $scope.searchMap.sortField=sortField;
	    $scope.searchMap.sort=sort;
	    $scope.search();
    }


	$scope.query4Page=function(pageNo){

		if(pageNo < 1 || pageNo>$scope.resultMap.totalPages){
			return;
		}
		$scope.searchMap.pageNo=pageNo;
		$scope.search();

	};
	
	//搜索
	$scope.search=function(){
	    $scope.searchMap.pageNo = parseInt($scope.searchMap.pageNo);
		searchService.search($scope.searchMap).success(
			function(response){
				$scope.resultMap=response;
				buildPageLabel();
			}
		);		
	};

	$scope.addSearchItem=function (key,value) {

		if(key == 'category' || key == 'brand' || key == 'price'){
			$scope.searchMap[key]=value;
		}else {
			$scope.searchMap.spec[key]=value;
		}
		$scope.search();
	}

	$scope.deleSearchItem=function (key) {
		if(key == 'category' || key == 'brand' || key == 'price'){
			$scope.searchMap[key]='';
		}else {
			delete $scope.searchMap.spec[key]
		}
		$scope.search();
	}
	$scope.isMinPage=function () {
        if($scope.searchMap.pageNo ==1){
            return true;
        }else {
            return false;
        }
    }
    $scope.isMaxPage=function () {
        if($scope.searchMap.pageNo == $scope.resultMap.totalPage){
            return true;
        }else {
            return false;
        }
    }

    $scope.reStart=function () {
        $scope.searchMap.category='';
        $scope.searchMap.brand='';
        $scope.searchMap.spec={};
        $scope.searchMap.price='';
        $scope.searchMap.pageNo=1;
        $scope.searchMap.pageSize=40;

    }
	
	
});