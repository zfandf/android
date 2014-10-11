Photo = {
    bucketTmpl: $('#J_PhotoGroupTmpl').html(),
    isBucket: 1,
    sortType: 'time',
    fnInit: function() {
        Photo.getPhotos();
    },
    getPhotos: function() {
        var oParam = {};
        oParam.data = {
            action: 'photo',
            is_bucket: Photo.isBucket,
            sort_type: Photo.sortType
        };
        oParam.callback = function(oData) {
            if (oData.code != 0) {
                return;
            }
            if (oParam.data.is_bucket == 1) {
                Photo.bucketCallback(oData);
            } else {
                Photo.photoCallback(oData);
            }
        }
        $.common.ajax(oParam);
    },

    bucketCallback: function(oData) {
        if (oData.buckets) {
            var str = $.common.setTemplate(Photo.bucketTmpl, oData.buckets);
            eList.html(str);
            Photo.initBucket();
        }
        
    },
    initBucket: function() {
        $('.J_BucketSelect').off('click').on('click', function() {
            $(this).next('.J_Photos J_Photo').addClass('select');
        });
        $('.J_Bucket').click(function(e) {
            var eBucket = $(this);
            
            var eSign = eBucket.find('.J_BucketSign');
            var ePhotoBox = eBucket.next('.J_Photos');

            if (eBucket.hasClass('minus')) {
                eBucket.removeClass('minus');
                eSign.removeClass('glyphicon-minus');
                ePhotoBox.addClass('hide');
            } else {
                eBucket.addClass('minus');
                eSign.addClass('glyphicon-minus');
                ePhotoBox.removeClass('hide');
            }
        });
        $('.J_Bucket:eq(0)').click();
    }
};