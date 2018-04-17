 //控制层 
app.controller('goodsController' ,function($scope,$controller,$location,goodsService,uploadService,itemCatService,typeTemplateService){
	
	$controller('baseController',{$scope:$scope});//继承
	
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
	    var id = $location.search()['id'];   //获取url上的参数

        if(id == null){
            return;
        }
		goodsService.findOne(id).success(
			function(response){
				$scope.entity= response;

				//向富文本添加商品介绍
                editor.html($scope.entity.goodsDesc.introduction);

                //读取图片
                $scope.entity.goodsDesc.itemImages = JSON.parse($scope.entity.goodsDesc.itemImages);

                //扩展属性
                $scope.entity.goodsDesc.customAttributeItems=  JSON.parse($scope.entity.goodsDesc.customAttributeItems);

                //规格
                $scope.entity.goodsDesc.specificationItems=JSON.parse($scope.entity.goodsDesc.specificationItems);

                //展示sku
                for( var i=0;i<$scope.entity.itemList.length;i++ ){
                    $scope.entity.itemList[i].spec =
                        JSON.parse( $scope.entity.itemList[i].spec);
                }
            }
		);

	}

	//判断是否被勾选
    $scope.checkAttributeValue = function (specName,optionName) {
        //获取当前对象的规格
	    var item = $scope.entity.goodsDesc.specificationItems;
        //查询是否存在
	    var object = $scope.searchObjectByKey(item,"attributeName",specName);

	    if(object ==null){
	        return null;
        }else{
	        if(object.attributeValue.indexOf(optionName)>=0){
                return true;
            }else {
	            return false;
            }
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
                    alert("保存成功!");

                    location.href="goods.html";//跳转到商品列表页
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
	
	
	$scope.uploadFile=function () {

		uploadService.uploadFile().success(
			function (response) {
				if(response.success){
					$scope.image_entity.url=response.message;
				}else {
					alert(response.message);
				}
            }) .error(function () {
			alert("上传发生错误");
        })
    }

    $scope.entity = {goods:{},goodsDesc:{itemImages:[],specificationItems:[]}};//定义页面实体结构

	//图片列表
    $scope.add_image_entity=function () {
    	//alert("列表");
		$scope.entity.goodsDesc.itemImages.push($scope.image_entity);
    }

    //列表中移除图片
    $scope.remove_image_entity=function(index){
       // alert("移除");
        $scope.entity.goodsDesc.itemImages.splice(index,1);
    }

    //一级列表
    $scope.selectItemCat1List = function () {
        itemCatService.findByParentId(0).success(
        	function (response) {
                $scope.itemCat1List=response;
            }
		)
    }

    //二级列表 参数一监控变量的值,触发的方法
	$scope.$watch('entity.goods.category1Id',function (newValue, oldValue) {

        itemCatService.findByParentId(newValue).success(
            function (response) {
                $scope.itemCat2List=response;
            }
        )
    })


    //三级列表 参数一监控变量的值,触发的方法
    $scope.$watch('entity.goods.category2Id',function (newValue, oldValue) {

        itemCatService.findByParentId(newValue).success(
            function (response) {
                $scope.itemCat3List=response;
            }
        )
    })

    //三级分类选择后  读取模板ID
	$scope.$watch('entity.goods.category3Id',function (newValue, oldValue) {

		itemCatService.findOne(newValue).success(
			function (response) {
				$scope.entity.goods.typeTemplateId = response.typeId; //更新模板ID
            }
		)
    })

    //模板ID选择后  更新品牌列表
	$scope.$watch('entity.goods.typeTemplateId',function (newValue, oldValue) {
        typeTemplateService.findOne(newValue).success(
        	function (response) {
				$scope.typeTemplate = response; //获取类型模板

                $scope.typeTemplate.brandIds = JSON.parse($scope.typeTemplate.brandIds);//品牌列表

                if($location.search()['id'] == null){  //如果没有ID，则加载模板中的扩展数据
                    $scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.typeTemplate.customAttributeItems); //扩展属性
                }

            }
		)

        //查询规格列表
        typeTemplateService.findSpecList(newValue).success(
            function(response){
                $scope.specList=response;
            }
        );

    })

	//添加或更新规格
	$scope.updateSpecAttribute=function ($event,name,value) {
		var object = $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems,'attributeName',name);

		if(object != null){
			//判断是否选择
			if($event.target.checked){
				object.attributeValue.push(value);
			}else {
				object.attributeValue.splice(object.attributeValue.indexOf(value),1);//移除选项

				//如果选择都取消将删除改点
				if(object.attributeValue.length == 0){
					$scope.entity.goodsDesc.specificationItems.splice(
						$scope.entity.goodsDesc.specificationItems.indexOf(object),1
					);

				}

			}
		}else {
            $scope.entity.goodsDesc.specificationItems.push(
                {"attributeName":name,"attributeValue":[value]});
		}

    }

    //添加增值列
	addColumn = function (list,columnName,columnValues) {
    	//alert(columnName);
		//alert(columnValues.length);
		var newList=[];//新的集合
        for(var i = 0;i< list.length;i++) {
            var oldRow = list[i];
			for (var j=0;j<columnValues.length;j++) {
				var newRow = JSON.parse(JSON.stringify(oldRow));  //深克隆
				newRow.spec[columnName]=columnValues[j];
				//alert(json.stringify(newRow));

				newList.push(newRow);
			}
        }
		return newList;
    }


    //创建sku列表
	$scope.createItemList=function () {
    	//alert("0");
		$scope.entity.itemList=[{spec:{},price:0,num:99999,status:'0',isDefault:'0'}];

		var item = $scope.entity.goodsDesc.specificationItems;

		//alert(item.length);

		for (var i=0;i<item.length;i++) {
            $scope.entity.itemList =
				addColumn($scope.entity.itemList,item[i].attributeName,item[i].attributeValue);
		}
    }

    $scope.status=['未审核','已审核','审核未通过','关闭'];//商品状态


	$scope.itemCatList=[];   //商品分类列表

	//加载商品分类列表
	$scope.findItemCatList=function () {
		itemCatService.findAll().success(
			function (response) {

				for (var i = 0;i < response.length;i++) {
                    $scope.itemCatList[response[i].id] = response[i].name;
				}
            }
		)
    }


});	
