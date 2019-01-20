"use strict";


/**
 * TODO Der Typ MessagesController dient zur Steuerung der Messages-View: •
 * Datei messages-controller.js (neu zu erstellen) • Templates: ◦
 * subjects-template ◦ messages-template ◦ message-output-template ◦
 * message-input-template
 */
 
(function () {
    const Controller = de_sb_messenger.Controller;

    const MessagesController = function () {
        Controller.call(this);
    };
    MessagesController.prototype = Object.create(Controller.prototype);
    MessagesController.prototype.constructor = MessagesController;

    /**
	 * Displays the associated view.
	 */
    Object.defineProperty(MessagesController.prototype, "display", {
        enumerable: false, 		// true if and only if this property shows up during enumeration of the properties on the corresponding object.
        
        configurable: false, 	// true if and only if the type of this property descriptor may be changed 
        						// and if the property may be deleted from the corresponding object.
        
        writable: true,			//true if and only if the value associated with the property may be changed with an assignment operator.
        
        value: function () {
            console.log("function display");
            if (!Controller.sessionOwner) return;
            	this.displayError();
            	
			try{
            	const mainElement = document.querySelector("main");
            	const subjectsElement = document.querySelector("#subjects-template").content.cloneNode(true).firstElementChild;
            	mainElement.appendChild(subjectsElement);
            	this.displayRootMessages();
            	const messageBox = document.querySelector(".messages");
            	console.log("MessageBox="+messageBox)
            	this.refreshAvatarSlider(subjectsElement.querySelector("span.slider"), Controller.sessionOwner.peopleObservingReferences, person => this.displayMessageEditor(messageBox, person.identity));
            } catch (error) {
				this.displayError(error);
			}	

        }
    });

    Object.defineProperty(MessagesController.prototype, "displayMessages", {
        enumerable: false,
        configurable: false,
        value: async function (parentMessageOutputElement, messages) {
            /**
            * TODO
                Die Instanz-Methode displayMessages() soll gegebene Nachrichten als Kinder des
                gegebenen DOM-Elements anzeigen. Ein Klick auf das Plus-Symbol neben dem AutorAvatar
                einer Nachricht soll die Methode toggleChildMessages() aufrufen. Ein
                Klick auf einen der Avatare soll dagegen die Methode displayMessageEditor()
                aufrufen um eine Nachricht zu erzeugen welche die gewählte Nachricht als subject
                assoziiert.
            */
			console.log("function displayMessages");
            const messageList = parentMessageOutputElement.querySelector("ul");
            while (messageList.firstChild) {
                messageList.removeChild(messageList.firstChild);
            }

            for (let message of messages) {
                // TODO: whats that?
                /*
                if (message.subjectReference !== Controller.sessionOwner.identity &&
                    Controller.sessionOwner.peopleObservedReferences.indexOf(message.subjectReference) < 0)
                    continue;
                */

                const messageOutputElement = document.querySelector("#message-output-template").content.cloneNode(true).firstElementChild;
                messageList.appendChild(messageOutputElement);

                const imageElement = messageOutputElement.querySelector("img");
                imageElement.src = "/services/people/" + message.authorReference + "/avatar";
                imageElement.addEventListener("click", event => this.displayMessageEditor(messageOutputElement, message.identity));

                const author = JSON.parse(await this.xhr("/services/people/" + message.authorReference, "GET", {"Accept": "application/json"}, "", "text"));
                messageOutputElement.querySelector("output.message-meta").innerHTML = author.email + " " + new Date(message.creationTimestamp).toLocaleString();
                messageOutputElement.querySelector("output.message-body").innerText = message.body;

                messageOutputElement.querySelector(".message-plus").addEventListener("click", event => this.toggleChildMessages(event, message, messageOutputElement));
            }

            /*
            messageList.appendChild(messageOutputElement);

            const imageElement = messageOutputElement.querySelector("img");
            imageElement.src = "/services/people/" + message.authorReference + "/avatar";
            imageElement.addEventListener("click", event => this.displayMessageEditor(messageOutputElement, message.identity));

            const author = JSON.parse(await this.xhr("/services/people/" + message.authorReference, "GET", {"Accept": "application/json"}, "", "text"));
            const mainSubject = JSON.parse(await this.xhr("/services/people/" + message.subjectReference, "GET", {"Accept": "application/json"}, "", "text"));
            messageOutputElement.querySelector("output.message-meta").innerHTML = author.email + " " + new Date(message.creationTimestamp).toLocaleString() + " <b>to: " + mainSubject.name.given + " " + mainSubject.name.family + "</b>";
            messageOutputElement.querySelector("output.message-body").innerText = message.body;

            messageOutputElement.querySelector(".message-plus").addEventListener("click", event => this.toggleChildMessages(event, message));
            */
        }
    });

    Object.defineProperty(MessagesController.prototype, "displayRootMessages", {
        enumerable: false,
        configurable: false,
        value: async function () {
            /**
			 * TODO Die Instanz-Methode displayRootMessages() soll diejenigen
			 * Nachrichten untereinander anzeigen welche als subject (sic!)
			 * entweder den aktuellen Benutzer oder eine von diesem beobachtete
			 * Person haben. Die Nachrichten werden dabei mittels GET
			 * /services/entities/{id}/messagesCaused abgefragt, gesammelt, und
			 * im Section-Element mit der CSS-Klasse „messages“ mittels
			 * displayMessages() angezeigt.
			 */
            console.log("function displayRootMessages");
            const mainElement = document.querySelector("main");
            mainElement.appendChild(document.querySelector("#messages-template").content.cloneNode(true).firstElementChild);

            const messageList = document.querySelector(".messages ul");
            let messages = JSON.parse(await this.xhr("/services/messages/", "GET", {"Accept": "application/json"}, "", "text"));

            for (let message of messages) {
                if (message.subjectReference !== Controller.sessionOwner.identity &&
                    Controller.sessionOwner.peopleObservedReferences.indexOf(message.subjectReference) < 0)
                    continue;

                const messageOutputElement = document.querySelector("#message-output-template").content.cloneNode(true).firstElementChild;
                messageList.appendChild(messageOutputElement);

                const imageElement = messageOutputElement.querySelector("img");
                imageElement.src = "/services/people/" + message.authorReference + "/avatar";
                imageElement.addEventListener("click", event => this.displayMessageEditor(messageOutputElement, message.identity));

                const author = JSON.parse(await this.xhr("/services/people/" + message.authorReference, "GET", {"Accept": "application/json"}, "", "text"));
                const mainSubject = JSON.parse(await this.xhr("/services/people/" + message.subjectReference, "GET", {"Accept": "application/json"}, "", "text"));
                messageOutputElement.querySelector("output.message-meta").innerHTML = author.email + " " + new Date(message.creationTimestamp).toLocaleString() + " <b>to: " + mainSubject.name.given + " " + mainSubject.name.family + "</b>";
                messageOutputElement.querySelector("output.message-body").innerText = message.body;

                messageOutputElement.querySelector(".message-plus").addEventListener("click", event => this.toggleChildMessages(event, message, messageOutputElement));
            }
        }
    });

    Object.defineProperty(MessagesController.prototype, "toggleChildMessages", {
        enumerable: false,
        configurable: false,
        value: async function (event, message, messageOutputElement) {
            /**
             * TODO
                 Die Instanz-Methode toggleChildMessages() soll eine Nachrichten Hierarchieebene
                 entweder ein- oder ausblenden, je nachdem ob sie bereits ein- oder
                 ausgeblendet ist. Wird eine Hierarchieebene eingeblendet, dann sollen diejenigen
                 Nachrichten welche die Parent-Message als subject besitzen mittels REST abgefragt,
                 und mittels displayMessages() zur Anzeige gebracht werden. Zum Ausblenden soll
                 displayMessages() dagegen mit einer leeren Message-Menge aufgerufen werden.
             */
			console.log("function toggleChildMessages");
            if (event.target.className == "message-plus") {
                event.target.className = "message-minus";

                // rest request
                let messages = JSON.parse(await this.xhr("/services/entities/" + message.identity + "/messagesCaused", "GET", {"Accept": "application/json"}, "", "text"))

                this.displayMessages(messageOutputElement, messages)
            } else {
                event.target.className = "message-plus";
                this.displayMessages(messageOutputElement, [])
            }
        }
    });

    Object.defineProperty(MessagesController.prototype, "displayMessageEditor", {
        enumerable: false,
        configurable: false,
        value: async function (parentElement, subjectIdentity) {
            /**
			 * TODO Die Instanz-Methode displayMessageEditor(parentElement,
			 * subjectIdentity) soll einen Nachrichten-Editor auf der gewählten
			 * Hierarchieebene einblenden. Die Nachricht soll dabei das gegebene
			 * subject, und den aktuellen Benutzer als author erhalten. Bei
			 * Klick auf deren Sende-Knopf soll die Nachricht mittels
			 * persistMessage() gespeichert, und die Hierarchieebene frisch
			 * geladen werden.
			 */
			
            console.log(parentElement+" "+ subjectIdentity);
            console.log("function displayMessageEditor")
            const messageList = document.querySelector(".messages ul");
            const messageInputElement = document.querySelector("#message-input-template").content.cloneNode(true).firstElementChild;
                
            parentElement.appendChild(messageInputElement);
            // const person = JSON.parse(await this.xhr("/services/people/"+
			// subjectIdentity, "GET", {"Accept": "application/json"}, "",
			// "text"));

            const imageElement = messageInputElement.querySelector("img");
            imageElement.src = "/services/people/" + Controller.sessionOwner.identity + "/avatar";

            const buttonElement = messageInputElement.querySelector("button");
            buttonElement.addEventListener("click", event => this.persistMessage(messageInputElement, subjectIdentity));
			

            /*
			 * const anchorElement = document.createElement("a");
			 * anchorElement.appendChild(imageElement);
			 * anchorElement.appendChild(document.createTextNode(person.name.given));
			 * anchorElement.title = person.name.given + " " +
			 * person.name.family; anchorElement.addEventListener("click", event =>
			 * clickAction(person)); sliderElement.appendChild(anchorElement);
			 */
        }
    });

    Object.defineProperty(MessagesController.prototype, "persistMessage", {
        enumerable: false,
        configurable: false,
        value: async function (messageInputElement, subjectIdentity) {
        	console.log("function persistMessage")
            const message = messageInputElement.querySelector("textarea").value;
            await this.xhr("/services/messages/?subjectReference="+subjectIdentity, "POST", {"Accept": "application/json"}, message, "text");
        
            const messageList = document.querySelector(".messages ul");
            
            const messageOutputElement = document.querySelector("#message-output-template").content.cloneNode(true).firstElementChild;
            messageList.appendChild(messageOutputElement);

            const imageElement = messageOutputElement.querySelector("img");
            imageElement.src = messageInputElement.querySelector("img").src;
            imageElement.addEventListener("click", event => this.displayMessageEditor(messageOutputElement, subjectIdentity));
            messageOutputElement.querySelector("output.message-body").innerText = message;
            //const author = JSON.parse(await this.xhr("/services/people/" + message.authorReference, "GET", {"Accept": "application/json"}, "", "text"));
            const mainSubject = JSON.parse(await this.xhr("/services/entities/" + subjectIdentity, "GET", {"Accept": "application/json"}, "", "text"));
            messageOutputElement.querySelector("output.message-meta").innerHTML = Controller.sessionOwner.email + " " + new Date(Date.now()).toLocaleString();
            
            messageInputElement.parentNode.removeChild(messageInputElement);
            this.displayMessages(messageOutputElement, []);
        }
    });

    /**
	 * Perform controller callback registration during DOM load event handling.
	 */
    window.addEventListener("load", event => {
        const anchor = document.querySelector("header li:nth-of-type(2) > a");
        const controller = new MessagesController();
        anchor.addEventListener("click", event => controller.display());
    });

}());