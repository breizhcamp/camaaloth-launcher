//global vue filters
Vue.filter('formatBytes', function (bytes, decimals) {
    if (bytes === 0) return '0 o';
    var k = 1024,
        dm = decimals <= 0 ? 0 : decimals || 1,
        sizes = ['o', 'Ko', 'Mo', 'Go', 'To', 'Po', 'Eo', 'Zo', 'Yo'],
        i = Math.floor(Math.log(bytes) / Math.log(k));

    return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
})