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
            const avatarSlider = document.querySelector("#subjects-template").content.cloneNode(true).firstElementChild;
            mainElement.appendChild(avatarSlider);
            console.log(Controller.sessionOwner);
            this.refreshAvatarSlider(avatarSlider.querySelector("span.slider"), Controller.sessionOwner.peopleObservingReferences, person => this.displayMessageEditor(this, person.identity));
            mainElement.appendChild(document.querySelector("#messages-template").content.cloneNode(true).firstElementChild);
            this.displayRootMessages();

        }
    });

    Object.defineProperty(MessagesController.prototype, "displayMessages", {
        enumerable: false,
        configurable: false, // TODO attributes ripped from preferences display Session owner because idk
        value: function () {
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
        value: function (parentElement, subjectIdentity) {
            /**
             * TODO
                 Die Instanz-Methode displayMessageEditor(parentElement, subjectIdentity)
                 soll einen Nachrichten-Editor auf der gewählten Hierarchieebene einblenden. Die
                 Nachricht soll dabei das gegebene subject, und den aktuellen Benutzer als author
                 erhalten. Bei Klick auf deren Sende-Knopf soll die Nachricht mittels
                 persistMessage() gespeichert, und die Hierarchieebene frisch geladen werden.
             */
        }
    });

    Object.defineProperty(MessagesController.prototype, "persistMessage", {
        enumerable: false,
        configurable: false, // TODO attributes ripped from preferences display Session owner because idk
        value: function (messageElement, subjectIdentity) {
            /**
            * TODO
                Soll eine neue Nachricht mittels REST-Call speichern.
            */
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