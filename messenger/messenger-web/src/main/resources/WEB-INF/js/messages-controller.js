"use strict";


/**
 * TODO Der Typ MessagesController dient zur Steuerung der Messages-View:
     • Datei messages-controller.js (neu zu erstellen)
     • Templates:
         ◦ subjects-template
         ◦ messages-template
         ◦ message-output-template
         ◦ message-input-template
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
        enumerable: false,
        configurable: false, // TODO attributes ripped from welcome controller
        writable: true,
        value: function () {
            /**
             * TODO
                 Die Instanz-Methode display() stellt diese View (Messages-View) teilweise dar, und registriert die
                 Methode displayMessageEditor() als Callback für das Klicken auf einen der
                 Benutzer-Avatare im Avatar-Slider, zur Erzeugung einer Nachricht mit der gewählten
                 Person als subject. Des Weiteren wird die Methode displayRootMessages()
                 aufgerufen um die Darstellung der Seite zu vervollständigen.
             */

            if (!Controller.sessionOwner) return;
            this.displayError();

            const mainElement = document.querySelector("main");
            const subjectsElement = document.querySelector("#subjects-template").content.cloneNode(true).firstElementChild;
            mainElement.appendChild(subjectsElement);
            this.refreshAvatarSlider(subjectsElement.querySelector("span.slider"), Controller.sessionOwner.peopleObservingReferences, person => this.displayMessageEditor(this, person.identity));
            this.displayRootMessages();

        }
    });

    Object.defineProperty(MessagesController.prototype, "displayMessages", {
        enumerable: false,
        configurable: false, // TODO attributes ripped from preferences display Session owner because idk
        value: async function () {
            const messageList = document.querySelector(".messages ul");
            const messages = JSON.parse(await this.xhr("/services/messages/", "GET", {"Accept": "application/json"}, "", "text"));

            for (let message of messages) {
                const messageOutputElement = document.querySelector("#message-output-template").content.cloneNode(true).firstElementChild;
                messageList.appendChild(messageOutputElement);
                const imageElement = messageOutputElement.querySelector("img");
                imageElement.src = "/services/people/" + message.authorReference + "/avatar";
                //messageOutputElement.querySelector("output.message-meta").innerText;
                messageOutputElement.querySelector("output.message-body").innerText = message.body;
            }

            //const messageOutputElement = document.querySelector("#message-output-template").content.cloneNode(true).firstElementChild;
            //messageList.appendChild(messageOutputElement);


            /**
            * TODO
                Die Instanz-Methode displayMessages() soll gegebene Nachrichten als Kinder des
                gegebenen DOM-Elements anzeigen. Ein Klick auf das Plus-Symbol neben dem AutorAvatar
                einer Nachricht soll die Methode toggleChildMessages() aufrufen. Ein
                Klick auf einen der Avatare soll dagegen die Methode displayMessageEditor()
                aufrufen um eine Nachricht zu erzeugen welche die gewählte Nachricht als subject
                assoziiert.
            */
        }
    });

    Object.defineProperty(MessagesController.prototype, "displayRootMessages", {
        enumerable: false,
        configurable: false, // TODO attributes ripped from preferences display Session owner because idk
        value: function () {
            /**
            * TODO
                Die Instanz-Methode displayRootMessages() soll diejenigen Nachrichten
                untereinander anzeigen welche als subject (sic!) entweder den aktuellen Benutzer oder
                eine von diesem beobachtete Person haben. Die Nachrichten werden dabei mittels GET
                /services/entities/{id}/messagesCaused abgefragt, gesammelt, und im Section-Element
                mit der CSS-Klasse „messages“ mittels displayMessages() angezeigt.
            */
            const mainElement = document.querySelector("main");
            mainElement.appendChild(document.querySelector("#messages-template").content.cloneNode(true).firstElementChild);
            this.displayMessages();

        }
    });

    Object.defineProperty(MessagesController.prototype, "toggleChildMessages", {
        enumerable: false,
        configurable: false, // TODO attributes ripped from preferences display Session owner because idk
        value: function () {
            /**
             * TODO
                 Die Instanz-Methode toggleChildMessages() soll eine NachrichtenHierarchieebene
                 entweder ein- oder ausblenden, je nachdem ob sie bereits ein- oder
                 ausgeblendet ist. Wird eine Hierarchieebene eingeblendet, dann sollen diejenigen
                 Nachrichten welche die Parent-Message als subject besitzen mittels REST abgefragt,
                 und mittels displayMessages() zur Anzeige gebracht werden. Zum Ausblenden soll
                 displayMessages() dagegen mit einer leeren Message-Menge aufgerufen werden.
             */
        }
    });

    Object.defineProperty(MessagesController.prototype, "displayMessageEditor", {
        enumerable: false,
        configurable: false, // TODO attributes ripped from preferences display Session owner because idk
        value: async function (parentElement, subjectIdentity) {
            /**
             * TODO
                 Die Instanz-Methode displayMessageEditor(parentElement, subjectIdentity)
                 soll einen Nachrichten-Editor auf der gewählten Hierarchieebene einblenden. Die
                 Nachricht soll dabei das gegebene subject, und den aktuellen Benutzer als author
                 erhalten. Bei Klick auf deren Sende-Knopf soll die Nachricht mittels
                 persistMessage() gespeichert, und die Hierarchieebene frisch geladen werden.
             */

            const messageList = document.querySelector(".messages ul");
            const messageInputElement = document.querySelector("#message-input-template").content.cloneNode(true).firstElementChild;
            messageList.appendChild(messageInputElement);

            const person = JSON.parse(await this.xhr("/services/people/"+ subjectIdentity, "GET", {"Accept": "application/json"}, "", "text"));

            const imageElement = messageInputElement.querySelector("img");
            imageElement.src = "/services/people/" + Controller.sessionOwner.identity + "/avatar";

            const buttonElement = messageInputElement.querySelector("button");
            buttonElement.addEventListener("click", event => this.persistMessage(messageInputElement, subjectIdentity));


            /*            const anchorElement = document.createElement("a");
                        anchorElement.appendChild(imageElement);
                        anchorElement.appendChild(document.createTextNode(person.name.given));
                        anchorElement.title = person.name.given + " " + person.name.family;
                        anchorElement.addEventListener("click", event => clickAction(person));
                        sliderElement.appendChild(anchorElement);*/
        }
    });

    Object.defineProperty(MessagesController.prototype, "persistMessage", {
        enumerable: false,
        configurable: false, // TODO attributes ripped from preferences display Session owner because idk
        value: async function (messageElement, subjectIdentity) {
            const message = messageElement.querySelector("textarea").value;
            await this.xhr("/services/messages/?subjectReference="+subjectIdentity, "POST", {"Accept": "application/json"}, message, "text");
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