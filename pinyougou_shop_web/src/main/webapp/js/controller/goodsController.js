 //控制层 
app.controller('goodsController' ,function($scope,$controller,$location,goodsService,uploadService,itemCatService,typeTemplateService){
	
	$controller('baseController',{$scope:$scope});//继承


	$scope.itemCatList=[];

	$scope.status=['未审核','已审核','审核未通过','关闭'];//商品状态
	$scope.findItemCatList=function(){
		itemCatService.findAll().success(
			function (response) {
				for (var i = 0; i < response.length; i++) {
					$scope.itemCatList[response[i].id]=response[i].name;
				}
			}
		)

	}
	$scope.selectItemCat1List=function(){
		itemCatService.findByParentId(0).success(
			function (response) {
				$scope.itemCat1List=response;
				$scope.itemCat2List={};
				$scope.itemCat3List={};
			}
		)
	};

	$scope.$watch("entity.goods.category1Id",function (newValue, oldValue) {
		itemCatService.findByParentId(newValue).success(
			function (response) {
				$scope.itemCat2List=response;
				$scope.itemCat3List={};

			}
		)
	});


	$scope.$watch("entity.goods.category2Id",function (newValue, oldValue) {
		itemCatService.findByParentId(newValue).success(
			function (response) {
				$scope.itemCat3List=response;

			}
		)
	});

	$scope.$watch("entity.goods.category3Id",function (newValue, oldValue) {
		itemCatService.findOne(newValue).success(
			function (response) {
				$scope.entity.goods.typeTemplateId = response.tbTypeTemplate.id;
				// alert(response.tbTypeTemplate.id)
			}
		)
	});

	$scope.$watch("entity.goods.typeTemplateId",function (newValue, oldValue) {
		 typeTemplateService.findOne(newValue).success(
		 	function (response) {
				$scope.typeTemplate=response;
				$scope.typeTemplate.brandIds=JSON.parse($scope.typeTemplate.brandIds);
				// alert(JSON.parse($scope.typeTemplate.customAttributeItems));
				if($location.search()['id']==null) {
					$scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.typeTemplate.customAttributeItems);
				}
			}
		 )

		typeTemplateService.findSpecList(newValue).success(
			function (response) {
				$scope.specList=response;
			}
		)

	});



    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(){

		var id = $location.search()['id'];
		if(id == null){
			return ;
		}
		goodsService.findOne(id).success(
			function(response){
				$scope.entity= response;
				editor.html($scope.entity.goodsDesc.introduction);
				$scope.entity.goodsDesc.itemImages= JSON.parse($scope.entity.goodsDesc.itemImages);
				$scope.entity.goodsDesc.customAttributeItems= JSON.parse($scope.entity.goodsDesc.customAttributeItems);
				$scope.entity.goodsDesc.specificationItems=JSON.parse($scope.entity.goodsDesc.specificationItems);


				for (var i = 0; i < $scope.entity.itemList.length; i++) {
					$scope.entity.itemList[i].spec = JSON.parse($scope.entity.itemList[i].spec);
				}

			}
		);				
	};

	$scope.checkAttributeValue=function(specName,optionName){
		var items= $scope.entity.goodsDesc.specificationItems;
		var object =$scope.searchObjectByKey( items,'attributeName', specName);

		if(object!=null){
			if(object.attributeValue.indexOf(optionName)>=0){//如果能够查询到规格选项
				return true;
			}else{
				return false;
			}
		}else{
			return false;
		}
	}


	//保存 
	$scope.save=function(){
		$scope.entity.goodsDesc.introduction=editor.html();
		var serviceObject;//服务层对象
		if($scope.entity.goods.id!=null){//如果有ID
			serviceObject=goodsService.update( $scope.entity ); //修改  
		}else{
			serviceObject=goodsService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//重新查询 
		        	alert("保存成功");
					// $scope.entity={};
					// // editor.html("");
					// location.reload();
					location.href="goods.html";
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	//增加商品 
	$scope.add=function(){			
		$scope.entity.goodsDesc.introduction=editor.html();
		
		goodsService.add( $scope.entity  ).success(
			function(response){
				if(response.success){
					alert("新增成功");
					$scope.entity={};
					// editor.html("");//清空富文本编辑器
					location.reload();

				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		goodsService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//上传图片
	$scope.uploadFile=function() {
		uploadService.uploadFile().success(
			function (response) {
				if (response.success) {
					$scope.image_entity.url = response.message;
				} else {
					alert(response.message);
				}
			}).error(function() {
			alert("上传发生错误");
		});
	};
	
	$scope.entity={ goodsDesc:{itemImages:[],specificationItems:[]}  };

	$scope.updateSpecAttribute=function($event,name,value){
		var object= $scope.searchObjectByKey(
			$scope.entity.goodsDesc.specificationItems ,'attributeName', name); //遍历集合 查找是否包含value字段 没有返回null
		if(object!=null){
			if($event.target.checked ){
				object.attributeValue.push(value); // 复选框选中 集合中push该值
			}else{
				object.attributeValue.splice( object.attributeValue.indexOf(value ) ,1);//移除选项

				if(object.attributeValue.length==0){  // 没有值得时候删除这个集合对象
					$scope.entity.goodsDesc.specificationItems.splice(
						$scope.entity.goodsDesc.specificationItems.indexOf(object),1);
				}
			}
		}else{
			$scope.entity.goodsDesc.specificationItems.push(
				{"attributeName":name,"attributeValue":[value]}); //没有找到该字段 直接初始化新的集合
		}
	};


	$scope.createItemList=function(){  //选择规格复选框选项时触发
		$scope.entity.itemList = [{spec:{},price:0,num:99999,status:'0',isDefault:'0' } ];
		var items = $scope.entity.goodsDesc.specificationItems;  // 其实就是 itemList.spec
		for (var i = 0; i < items.length; i++) {
			$scope.entity.itemList = addColumn($scope.entity.itemList,items[i].attributeName,items[i].attributeValue);
		}
	};

	addColumn=function(list,columnName,columnValue){  //遍历itemList  columnName 其实就是规格名称 columnValue规格值
		var newList = []; //深度克隆 所以需要创建新的对象
		for (var i = 0; i < list.length; i++) {
			var oldList=list[i];  //被克隆对象的值
			for (var j = 0; j < columnValue.length; j++) { //遍历规格值得集合
				var newRow = JSON.parse(JSON.stringify(oldList)); //将被克隆对象转换为json字符串 再转换为对象 赋值给克隆对象（深度克隆）
				newRow.spec[columnName] = columnValue[j]; // 将被克隆的规格值 赋值给 新的列当列名
				newList.push(newRow);  // 克隆对象放到空的集合里
			}
		}
		return newList; //返回克隆好的集合对象
	};

	$scope.turnPage=function(id,isMarketable){
		if(isMarketable=== '1'){
			alert("请先下架商品")
		}else {
			location.href="goods_edit.html#?id="+id;
		}
	};


	//将当前上传的图片实体存入图片列表
	$scope.add_image_entity=function(){
		$scope.entity.goodsDesc.itemImages.push($scope.image_entity);			
	};
	
	//移除图片
	$scope.remove_image_entity=function(index){
		$scope.entity.goodsDesc.itemImages.splice(index,1);
	}

	$scope.goods2up=function () {
		if(confirm("确定要上/下架所选商品？")){
			goodsService.isAuditStatus($scope.selectIds).success(
				function (response) {
					if(response.success){
						// 审核过商品
						goodsService.isMarketable($scope.selectIds).success(
							function (data) {
								if (data.success){
									//修改成功
									alert("商品上/下架成功");
									$scope.reloadList();//刷新列表
									$scope.selectIds=[];
								}else {
									//修改失败
									alert(data.message)
								}
							}
						)
					}else {
						//审核没过商品
						alert(response.message);
					}
				}
			)

		}
	}

});	
