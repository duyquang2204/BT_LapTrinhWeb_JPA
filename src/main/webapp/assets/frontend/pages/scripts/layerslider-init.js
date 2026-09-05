var LayersliderInit = function () {
    return {
        initLayerSlider: function () {
            $('#layerslider').layerSlider({
                skinsPath: 'assets/global/plugins/slider-layer-slider/skins/',
                skin: 'fullwidth',
                thumbnailNavigation: 'hover',
                hoverPrevNext: false,
                responsiveUnder: 940,
                layersContainer: 940
            });
        }
    };
}();