"use strict";


/**
 * TODO Der Typ MessagesController dient zur Steuerung der Messages-View: •
 * Datei messages-controller.js (neu zu erstellen) • Templates: ◦
 * subjects-template ◦ messages-template ◦ message-output-template ◦
 * message-input-template
 */

(function() {
	const Controller = de_sb_messenger.Controller;

	const MessagesController = function() {
		Controller.call(this);
		this.inputBox = null;
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

		value: function() {
			if (!Controller.sessionOwner) {
				const anchor = document.querySelector("header li:nth-of-type(1) > a");
				anchor.dispatchEvent(new MouseEvent("click"));
				return;
			}

			this.displayError();
			try {
				const mainElement = document.querySelector("main");
				const subjectsElement = document.querySelector("#subjects-template").content.cloneNode(true).firstElementChild;
				mainElement.appendChild(subjectsElement);
				this.displayRootMessages();
				const messageBox = document.querySelector(".messages");
				this.refreshAvatarSlider(subjectsElement.querySelector("span.slider"), Controller.sessionOwner.peopleObservingReferences, person => this.displayMessageEditor(messageBox, person.identity));
			} catch (error) {
				this.displayError(error);
			}
		}
	});

	Object.defineProperty(MessagesController.prototype, "displayMessages", {
		enumerable: false,
		configurable: false,
		value: async function(parentMessageOutputElement, messages) {
            /**
            *
                Die Instanz-Methode displayMessages() soll gegebene Nachrichten als Kinder des
                gegebenen DOM-Elements anzeigen. Ein Klick auf das Plus-Symbol neben dem AutorAvatar
                einer Nachricht soll die Methode toggleChildMessages() aufrufen. Ein
                Klick auf einen der Avatare soll dagegen die Methode displayMessageEditor()
                aufrufen um eine Nachricht zu erzeugen welche die gewählte Nachricht als subject
                assoziiert.
            */
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

				const responseAuthor = await fetch("/services/people/" + message.authorReference, { method: "GET", headers: { "Accept": "application/json" }, credentials: "include" });
				if (!responseAuthor.ok) throw new Error("HTTP " + responseAuthor.status + " " + responseAuthor.statusText);
				const author = await responseAuthor.json();
				messageOutputElement.querySelector("output.message-meta").innerHTML = author.email + " " + new Date(message.creationTimestamp).toLocaleString();
				messageOutputElement.querySelector("output.message-body").innerText = message.body;

				messageOutputElement.querySelector(".message-plus").addEventListener("click", event => this.toggleChildMessages(event, message, messageOutputElement));
			}
		}
	});

	Object.defineProperty(MessagesController.prototype, "displayRootMessages", {
		enumerable: false,
		configurable: false,
		value: async function() {
            /**
			 * TODO Die Instanz-Methode displayRootMessages() soll diejenigen
			 * Nachrichten untereinander anzeigen welche als subject (sic!)
			 * entweder den aktuellen Benutzer oder eine von diesem beobachtete
			 * Person haben. Die Nachrichten werden dabei mittels GET
			 * /services/entities/{id}/messagesCaused abgefragt, gesammelt, und
			 * im Section-Element mit der CSS-Klasse „messages“ mittels
			 * displayMessages() angezeigt.
			 */
			const mainElement = document.querySelector("main");
			mainElement.appendChild(document.querySelector("#messages-template").content.cloneNode(true).firstElementChild);

			const messageList = document.querySelector(".messages ul");
			// TODO: new messages above
			/* TODO make site only show right messages

				let responsePromises = [];
			    personReferences = array of sessionOwnerId and sessionOwner.peopleObservedIds
			    for (let personReference of personReferences) {
				    responsePromises.push(fetch("/services/entities/" + personReference + "/messagesCaused"));
				}
				
				let rootMessages = [];
				for (let responsePromise of responsePromises) {
					let response = await responsePromise;
					if (!response.ok) continue;
					const messages = await response.json();
					rootMessages.push.apply(rootMessages, messages);
				}
				rootMessages.sort((l, r) => l.identity - r.identity);
			*/
			const responseMessages = await fetch("/services/messages/", {methods: "GET", headers:{ "Accept": "application/json" }, credentials: "include"});
			if (!responseMessages.ok) throw new Error("HTTP " + responseMessages.status + " " + responseMessages.statusText);
			let messages = await responseMessages.json();
			for (let message of messages) {
				if (message.subjectReference !== Controller.sessionOwner.identity &&
					Controller.sessionOwner.peopleObservedReferences.indexOf(message.subjectReference) < 0)
					continue;

				const messageOutputElement = document.querySelector("#message-output-template").content.cloneNode(true).firstElementChild;
				messageList.appendChild(messageOutputElement);

				const imageElement = messageOutputElement.querySelector("img");
				imageElement.src = "/services/people/" + message.authorReference + "/avatar";
				imageElement.addEventListener("click", event => this.displayMessageEditor(messageOutputElement, message.identity));

				const responseAuthor = await fetch("/services/people/" + message.authorReference, { method: "GET", headers: { "Accept": "application/json" } });
				if (!responseAuthor.ok) throw new Error("HTTP " + responseAuthor.status + " " + responseAuthor.statusText);
				const author = await responseAuthor.json();
				const responseMainSubject = await fetch("/services/people/" + message.subjectReference, { method: "GET", headers: { "Accept": "application/json" }, credentials: "include" });
				if (!responseMainSubject.ok) throw new Error("HTTP " + responseMainSubject.status + " " + responseMainSubject.statusText);
				const mainSubject = await responseMainSubject.json();
				messageOutputElement.querySelector("output.message-meta").innerHTML = author.email + " " + new Date(message.creationTimestamp).toLocaleString() + " <b>to: " + mainSubject.name.given + " " + mainSubject.name.family + "</b>";
				messageOutputElement.querySelector("output.message-body").innerText = message.body;

				messageOutputElement.querySelector(".message-plus").addEventListener("click", event => this.toggleChildMessages(event, message, messageOutputElement));
			}
		}
	});

	Object.defineProperty(MessagesController.prototype, "toggleChildMessages", {
		enumerable: false,
		configurable: false,
		value: async function(event, message, messageOutputElement) {

			if (event.target.className == "message-plus") {
				event.target.className = "message-minus";

				// rest request
				// TODO uri auslagern
				const uri = 
				const responseMessages = await fetch("/services/entities/" + message.identity + "/messagesCaused", { method: "GET", headers: { "Accept": "application/json" }, credentials: "include" });
				if (!responseMessages.ok) throw new Error("HTTP " + responseMessages.status + " " + responseMessages.statusText);
				let messages = await responseMessages.json();
				
				this.displayMessages(messageOutputElement, messages)
			} else {
				event.target.className = "message-plus";
				this.displayMessages(messageOutputElement, [])
			}
		}
	});

	Object.defineProperty(MessagesController.prototype, "refreshInputBox", {
		enumerable: false,
		configurable: false,
		writable: false,
		value: async function(newInputBox, messageList) {
			if (this.inputBox != null) {
				if (this.inputBox.parentElement != null) {
					this.inputBox.parentNode.removeChild(this.inputBox);
				}
			}
			this.inputBox = newInputBox;
			messageList.prepend(newInputBox);
		}
	});

	Object.defineProperty(MessagesController.prototype, "displayMessageEditor", {
		enumerable: false,
		configurable: false,
		value: async function(parentElement, subjectIdentity) {

			const messageList = parentElement.querySelector("ul");
			const messageInputElement = document.querySelector("#message-input-template").content.cloneNode(true).firstElementChild;

			this.refreshInputBox(messageInputElement, messageList);

			const imageElement = messageInputElement.querySelector("img");
			imageElement.src = "/services/people/" + Controller.sessionOwner.identity + "/avatar";

			const buttonElement = messageInputElement.querySelector("button");
			buttonElement.addEventListener("click", event => this.persistMessage(messageInputElement, subjectIdentity));
		}
	});

	Object.defineProperty(MessagesController.prototype, "persistMessage", {
		enumerable: false,
		configurable: false,
		value: async function(messageInputElement, subjectIdentity) {
			const messageBody = messageInputElement.querySelector("textarea").value;
			// TODO uri auslagern überall
			const createResponse = await fetch("/services/messages/?subjectReference=" + subjectIdentity, {method: "POST", headers: {"Content-Type": "text/plain"}, body: messageBody, credentials: "include"});
			if (!createResponse.ok) throw new Error("HTTP " + createResponse.status + " " + createResponse.statusText);

			const newIdentity = parseInt(await createResponse.text());
			const messageOutputElement = document.querySelector("#message-output-template").content.cloneNode(true).firstElementChild;
			const parent = messageInputElement.parentNode;
			parent.appendChild(messageOutputElement);

			const imageElement = messageOutputElement.querySelector("img");
			imageElement.src = messageInputElement.querySelector("img").src;
			imageElement.addEventListener("click", event => this.displayMessageEditor(messageOutputElement, newIdentity));

			// TODO innerText ersetzen mit outputElement/value
			messageOutputElement.querySelector("output.message-body").value = message;
			messageOutputElement.querySelector("output.message-meta").value = Controller.sessionOwner.email + " " + new Date(Date.now()).toLocaleString();

			parent.removeChild(messageInputElement);
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