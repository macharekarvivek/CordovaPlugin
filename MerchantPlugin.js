cordova.define("android.mp.MerchantPlugin",

 function(require, exports, module) {

  //-------------------------------------------------------------------------------------------
  //-------------------------------------------------------------------------------------------
  var exec = require('cordova/exec');
  //INTERFACE BRIDGE method for set app info
  exports.setAppInfoMethod = function(clientParams, successCallback, errorCallback) {

   exec(

    function(result) {
     successCallback(result);
    },
    function(error) {
    //alert("on error");
     errorCallback(error);
    }, 'MerchantPlugin', 'setAppInfoMethod', [clientParams]

   );
  };

  //-------------------------------------------------------------------------------------------
  //-------------------------------------------------------------------------------------------
  //INTERFACE BRIDGE method for make payment

     exports.paymentCallMethod = function(clientParams, successCallback, errorCallback) {

       exec(

        function(result) {
        //alert(JSON.stringify(result));
         successCallback(result);
        },
        function(error) {
         //alert(JSON.stringify(error));
         errorCallback(error);
        }, 'MerchantPlugin', 'paymentCallMethod', [clientParams]

       );
      };
  //-------------------------------------------------------------------------------------------
  //-------------------------------------------------------------------------------------------

 });