var VisualRecognitionV3 = require('watson-developer-cloud/visual-recognition/v3');
var fs = require('fs');
// Load the Cloudant library
// Load the Cloudant library.

var Cloudant = require('@cloudant/cloudant');
var cloudant = new Cloudant({ url: 'https://6f940a9b-85c7-452c-87a5-a76e88102aec-bluemix:2cfc65dbdf7e42faff5e21f58202dfe46ff029061823f2fa3e1d00c48fa60cde@6f940a9b-85c7-452c-87a5-a76e88102aec-bluemix.cloudantnosqldb.appdomain.cloud', plugins: { iamauth: { iamApiKey: 'Vuw_uc7_ttadwLwxT84FX-VAcuIWccqo1pJrzC1IC_Rh' } } });
var db = cloudant.db.use('indanger')
var visualRecognition = new VisualRecognitionV3({
	version: '2018-03-19',
	iam_apikey: 'T-ZY19CD0OsHAPzdCb38D9R9x0nfKeWA29DLZ5Vagbjb'
});

for(var i=0;i<100;i++)
{
var images_file= fs.createReadStream("/Users/mathikshara/Documents/AngelHack/Project/img/watson-node-starter/pizza_"+i+".jpg");
}
var classifier_ids = ["DefaultCustomModel_1124562151"];
var threshold = 0.6;

var params = {
	images_file: images_file,
	classifier_ids: classifier_ids,
	threshold: threshold
};

visualRecognition.classify(params, function(err, response) {
	if (err) { 
		console.log(err);
	} else {
        if(parseFloat(response.images[0].classifiers[0].classes[0].score,10)>0.5)
        {
            console.log("Flooded")
            // cloudant.db.list(function(err, body) {
            //     body.forEach(function(db) {
            //      console.log(db);
            //      });
            //     });

            var createDocument = function(callback) {
                console.log("Creating document 'mydoc'");
                // specify the id of the document so you can update and delete it later
                db.insert({ _id: 'mydoc', a:response.images[1].names }, function(err, data) {
                  console.log('Error:', err);
                  console.log('Data:', data);
                  callback(err, data);
                });
              };
              createDocument();
        }
        else
        {
            console.log("Not flooded")
        }
        console.log(JSON.stringify(parseFloat(response.images[0].classifiers[0].classes[0].score,10), null, 2))
        
	}
});