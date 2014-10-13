Photo = {
    bucketTmpl: $('#J_PhotoGroupTmpl').html(),
    photoTmpl: $('#J_PhotoTmpl').html(),
    sortType: 0, // 0 相册， 1 时间
    fnInit: function() {
        Photo.initEvent();
    },
    initEvent: function() {
        $('.J_PhotoCurmb').off('click').on('click', function() {
            var sort_type = $(this).attr('sort_type');
            Photo.sortType = sort_type;
            Photo.getPhotos();
        });
        $('.J_PhotoCurmb:eq(0)').click();
    },
    getPhotos: function(bucket_id) {
        var oParam = {};
        bucket_id = bucket_id || 0;
        oParam.data = {
            action: 'photo',
            bucket_id: bucket_id,
            sort_type: Photo.sortType
        };
        oParam.callback = function(oData) {
            if (oData.code != 0) {
                return;
            }
            if (bucket_id) {
                Photo.photoCallback(bucket_id, oData);
            } else {
                Photo.bucketCallback(oData);
            }
        }
        $.common.ajax(oParam);
    },

    photoCallback: function(bucket_id, oData) {
        if (oData.photos) {
            var str = $.common.setTemplate(Photo.photoTmpl, oData.photos);
            ePhotoBox = $('.J_Photos[bucket_id="'+bucket_id+'"]');
            ePhotoBox.html(str);
            Photo.initPhoto(bucket_id);

        }
        
    },
    initPhoto: function(bucket_id) {
        var ePhotos = $('.J_Photos[bucket_id="'+bucket_id+'"]');
        // var eBucketSelect = $('.J_BucketSelect[bucket_id="'+bucket_id+'"]');
        var ePhotoSelect = ePhotos.find('.J_SelectPhoto');
        ePhotoSelect.off('click').on('click', function() {
            var path = $(this).attr('path');
            var ePhoto = $('.J_Photo[path="'+path+'"]');
            if (ePhoto.hasClass('selected')) {
                ePhoto.removeClass('selected');
            } else {
                ePhoto.addClass('selected');
            }
        });
    },
    bucketCallback: function(oData) {
        if (oData.photos) {
            var str = $.common.setTemplate(Photo.bucketTmpl, oData.photos);
            eList.html(str);
            Photo.initBucket();
        }
    },
    initBucket: function() {
        $('.J_BucketSelect').off('click').on('click', function() {
            var bucket_id = $(this).attr('bucket_id');
            if ($(this).hasClass('selected')) {
                $(this).removeClass('selected');
                $('.J_Photos[bucket_id="'+bucket_id+'"] .J_Photo').removeClass('selected');
                return;
            }
            $(this).addClass('selected');
            $('.J_Photos[bucket_id="'+bucket_id+'"] .J_Photo').addClass('selected');
        });
        
        $('.J_Bucket').click(function(e) {
            var eBucket = $(this);
            
            var eSign = eBucket.find('.J_BucketSign');
            var ePhotoBox = eBucket.next('.J_Photos');
            var bucket_id = $(this).attr('bucket_id');
            if (eBucket.hasClass('minus')) {
                eBucket.removeClass('minus');
                eSign.removeClass('glyphicon-minus');
                ePhotoBox.addClass('hide');
            } else {
                eBucket.addClass('minus');
                eSign.addClass('glyphicon-minus');
                ePhotoBox.removeClass('hide');
                if (ePhotoBox.children().length == 0) {
                    Photo.getPhotos(bucket_id);
                }
            }
        });
        $('.J_Bucket:eq(0)').click();
    }
};