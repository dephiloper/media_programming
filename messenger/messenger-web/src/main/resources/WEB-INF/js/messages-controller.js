"use strict";


(function () {
    const Controller = de_sb_messenger.Controller;

    const MessagesController = function () {
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

        value: function () {
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
                this.refreshAvatarSlider(subjectsElement.querySelector("span.slider"),
                                         Controller.sessionOwner.peopleObservedReferences,
                                         person => this.displayMessageEditor(messageBox, person.identity));
            } catch (error) {
                this.displayError(error);
            }
        }
    });

    Object.defineProperty(MessagesController.prototype, "displayMessages", {
        enumerable: false,
        configurable: false,
        value: async function (parentMessageOutputElement, messages) {
            const messageList = parentMessageOutputElement.querySelector("ul");

            while (messageList.firstChild)
                messageList.removeChild(messageList.firstChild);

            for (let message of messages) {
                const messageOutputElement = document.querySelector("#message-output-template").content.cloneNode(true).firstElementChild;
                messageList.appendChild(messageOutputElement);

                const imageElement = messageOutputElement.querySelector("img");
                imageElement.src = "/services/people/" + message.authorReference + "/avatar";
                imageElement.addEventListener("click", event => this.displayMessageEditor(messageOutputElement, message.identity));

                const uri = "/services/people/" + message.authorReference;
                const responseAuthor = await fetch(uri, {
                    method: "GET",
                    headers: {"Accept": "application/json"},
                    credentials: "include"
                });

                if (!responseAuthor.ok) throw new Error("HTTP " + responseAuthor.status + " " + responseAuthor.statusText);

                const author = await responseAuthor.json();
                messageOutputElement.querySelector("output.message-meta").value = author.email + " " + new Date(message.creationTimestamp).toLocaleString();
                messageOutputElement.querySelector("output.message-body").value = message.body;
                messageOutputElement.querySelector(".message-plus").addEventListener("click", event => this.toggleChildMessages(event.target, message.identity, messageOutputElement));
            }
        }
    });

    Object.defineProperty(MessagesController.prototype, "displayRootMessages", {
        enumerable: false,
        configurable: false,
        value: async function () {
            const mainElement = document.querySelector("main");
            mainElement.appendChild(document.querySelector("#messages-template").content.cloneNode(true).firstElementChild);
            const messageList = document.querySelector(".messages ul");

            let responsePromises = [];
            const personReferences = Controller.sessionOwner.peopleObservedReferences.concat(Controller.sessionOwner.identity);

            for (let personReference of personReferences) {
                responsePromises.push(fetch("/services/entities/" + personReference + "/messagesCaused"));
            }

            let rootMessages = [];
            for (let responsePromise of responsePromises) {
                const response = await responsePromise;
                if (!response.ok) continue;
                const messages = await response.json();
                rootMessages.push.apply(rootMessages, messages);
            }
            rootMessages.sort((l, r) => l.identity - r.identity);

            for (let message of rootMessages) {
                const messageOutputElement = document.querySelector("#message-output-template").content.cloneNode(true).firstElementChild;
                messageList.appendChild(messageOutputElement);

                const imageElement = messageOutputElement.querySelector("img");
                imageElement.src = "/services/people/" + message.authorReference + "/avatar";
                imageElement.addEventListener("click", event => this.displayMessageEditor(messageOutputElement, message.identity));

                const uriAuthor = "/services/people/" + message.authorReference;
                const responseAuthor = await fetch(uriAuthor, {
                    method: "GET",
                    headers: {"Accept": "application/json"}
                });
                if (!responseAuthor.ok) throw new Error("HTTP " + responseAuthor.status + " " + responseAuthor.statusText);
                const author = await responseAuthor.json();

                const uriMainSubject = "/services/people/" + message.subjectReference;
                const responseMainSubject = await fetch(uriMainSubject, {
                    method: "GET",
                    headers: {"Accept": "application/json"},
                    credentials: "include"
                });
                if (!responseMainSubject.ok) throw new Error("HTTP " + responseMainSubject.status + " " + responseMainSubject.statusText);
                const mainSubject = await responseMainSubject.json();

                messageOutputElement.querySelector("output.message-meta").value = author.email + " " + new Date(message.creationTimestamp).toLocaleString();
                messageOutputElement.querySelector("output.message-body").value = message.body;
                messageOutputElement.querySelector(".message-plus").addEventListener("click", event => this.toggleChildMessages(event.target, message.identity, messageOutputElement));
            }
        }
    });

    Object.defineProperty(MessagesController.prototype, "toggleChildMessages", {
        enumerable: false,
        configurable: false,
        value: async function (event_target, message_identity, messageOutputElement) {

            if (event_target.className === "message-plus") {
                event_target.className = "message-minus";
                messageOutputElement.classList.add("expanded");

                const uri = "/services/entities/" + message_identity + "/messagesCaused";
                // try { // TODO inspect this part --> try it w/o try-catch
                const response = await fetch(uri, {
                    method: "GET",
                    headers: {"Accept": "application/json"},
                    credentials: "include"
                });
                /*
                } catch (e) {
                    return;
                }
                */
                if (!response.ok) throw new Error("HTTP " + response.status + " " + response.statusText);
                let messages = await response.json();

                this.displayMessages(messageOutputElement, messages);
            } else {
                event_target.className = "message-plus";
                messageOutputElement.classList.remove("expanded");
                this.displayMessages(messageOutputElement, [])
            }
        }
    });

    Object.defineProperty(MessagesController.prototype, "refreshInputBox", {
        enumerable: false,
        configurable: false,
        writable: false,
        value: async function (newInputBox, messageList) {
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
        value: async function (parentElement, subjectIdentity) {

            const messageList = parentElement.querySelector("ul");
            const messageInputElement = document.querySelector("#message-input-template").content.cloneNode(true).firstElementChild;

            this.refreshInputBox(messageInputElement, messageList);

            const imageElement = messageInputElement.querySelector("img");
            imageElement.src = "/services/people/" + Controller.sessionOwner.identity + "/avatar";

            const buttonElement = messageInputElement.querySelector("button");
            buttonElement.addEventListener("click", event => this.persistMessageAndExpand(messageInputElement, subjectIdentity));
        }
    });

    Object.defineProperty(MessagesController.prototype, "persistMessageAndExpand", {
        enumerable: false,
        configurable: false,
        value: async function (messageInputElement, subjectIdentity) {
            this.persistMessage(messageInputElement, subjectIdentity);

            const parent = messageInputElement.parentElement.parentElement;

            if (parent.className == "message") {
                const plus_elem = parent.querySelector(".message-plus")
                if (plus_elem) {
                    const messageIdentity = subjectIdentity;
                    const outputElement = parent;
                    this.toggleChildMessages(plus_elem, messageIdentity, outputElement);
                }
            }
        }
    });

    Object.defineProperty(MessagesController.prototype, "persistMessage", {
        enumerable: false,
        configurable: false,
        value: async function (messageInputElement, subjectIdentity) {
            const parent = messageInputElement.parentNode;
            const messageBody = messageInputElement.querySelector("textarea").value;
            const uri = "/services/messages/?subjectReference=" + subjectIdentity;
            const createResponse = await fetch(uri, {
                method: "POST",
                headers: {"Content-Type": "text/plain"},
                body: messageBody,
                credentials: "include"
            });
            if (!createResponse.ok) throw new Error("HTTP " + createResponse.status + " " + createResponse.statusText);

            const newIdentity = parseInt(await createResponse.text());
            const messageOutputElement = document.querySelector("#message-output-template").content.cloneNode(true).firstElementChild;
            parent.appendChild(messageOutputElement);

            const message_plus = messageOutputElement.querySelector(".message-plus");

            const imageElement = messageOutputElement.querySelector("img");
            imageElement.src = messageInputElement.querySelector("img").src;
            imageElement.addEventListener("click", () => this.displayMessageEditor(messageOutputElement, newIdentity));

            messageOutputElement.querySelector("output.message-body").value = messageBody;
            messageOutputElement.querySelector("output.message-meta").value = Controller.sessionOwner.email + " " + new Date(Date.now()).toLocaleString();
            message_plus.addEventListener("click", event => this.toggleChildMessages(event.target, newIdentity, messageOutputElement));

            if (messageInputElement.parentNode != null) {
                parent.removeChild(messageInputElement);
            }
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