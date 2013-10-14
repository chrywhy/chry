(function() {
    $(document).ready(function() {
        var editModel = new TextModel();
        var shareEditor = new ShareEditor({model:editModel});
    });

    var eleEditArea = $('#editArea');
    
    ShareEditor = Backbone.View.extend({
        el: $("#editor"),
        events: {
            "input #editArea": "onTextInput",
            "propertyChange #editArea": "onTextInput"
        },

        initialize: function () {
            this.listenTo(this.model, "change", this.onTextChanged);
        },
                
        onTextChanged : function() {
            eleEditArea.val(this.model.getText());
        },

        onTextInput : function() {
            var text = eleEditArea.val();
            this.model.updateText(text);
        }
    });
        
    TextModel = Backbone.Model.extend({
        defaults : {
            text : "",
            url: ""
        },
        websocket : null,
        
        getUri: function() {
            var rootUrl =  "ws://" + (document.location.hostname == "" ? "localhost" : document.location.hostname) + ":" +
                    (document.location.port == "" ? "8080" : document.location.port);
            return rootUrl + "/register";
        },

        initialize: function () {
            that = this;
            var websocket = new WebSocket(this.getUri());
            websocket.onopen = function (evt) {
            };
            websocket.onmessage = function (evt) {
                that.set({text: evt.data});
            };
            websocket.onerror = function (evt) {
            };
            this.websocket = websocket;
        },
                
        getText: function() {
            return this.get("text");
        },
                
        updateText : function(text) {
            this.text = text;
            this.websocket.send(text);
        }
    });
})();
