/**
 * de_sb_messenger.PeopleController: messenger people controller.
 * Copyright (c) 2013 Sascha Baumeister
 */
"use strict";

function createResourceWithQueryParameters(resource, queryParameters) {
	var res = resource + "?";
	Object.keys(queryParameters).forEach(function(paramName) {
		if (queryParameters[paramName] !== "") {
			res = res + paramName + "=" + queryParameters[paramName] + "&";
		}
	});
	return res.slice(0, -1) // remove last &
}

(function () {
	// imports
	const Controller = de_sb_messenger.Controller;

	/**
	 * Creates a new people controller that is derived from an abstract controller.
	 */
	const PeopleController = function () {
		Controller.call(this);
		this.searchResult = null;
	};
	PeopleController.prototype = Object.create(Controller.prototype);
	PeopleController.prototype.constructor = PeopleController;


	/**
	 * Displays the associated view.
	 */
	Object.defineProperty(PeopleController.prototype, "display", {
		enumerable: false,
		configurable: false,
		writable: true,
		value: function () {
			if (!Controller.sessionOwner) return;
			this.displayError();

			try {
				const observingElement = document.querySelector("#people-observing-template").content.cloneNode(true).firstElementChild;
				const observedElement = document.querySelector("#people-observed-template").content.cloneNode(true).firstElementChild;
				this.refreshAvatarSlider(observingElement.querySelector("span.slider"), Controller.sessionOwner.peopleObservingReferences, person => this.toggleObservation(person.identity));
				this.refreshAvatarSlider(observedElement.querySelector("span.slider"), Controller.sessionOwner.peopleObservedReferences, person => this.toggleObservation(person.identity));

				const mainElement = document.querySelector("main");
				mainElement.appendChild(observingElement);
				mainElement.appendChild(observedElement);
				mainElement.appendChild(document.querySelector("#candidates-template").content.cloneNode(true).firstElementChild);
				mainElement.querySelector("button").addEventListener("click", event => this.queryPeople());
			} catch (error) {
				this.displayError(error);
			}
		}
	});


	/**
	 * Performs a REST based filter criteria query for matching people, and refreshes
	 * the people view's bottom avatar slider with the result.
	 */
	Object.defineProperty(PeopleController.prototype, "queryPeople", {
		enumerable: false,
		configurable: false,
		value: async function () {
			// Read Criteria Fields
			const inputElements = document.querySelectorAll("section.candidates input");
			let queryParameters = {
				"email": inputElements[0].value.trim(),
				"forename": inputElements[1].value.trim(),
				"surname": inputElements[2].value.trim(),
				"street": inputElements[3].value.trim(),
				"city": inputElements[4].value.trim(),
			};

			const mainElement = document.querySelector("main");

			if (this.searchResult !== null && mainElement.contains(this.searchResult)) {
				mainElement.removeChild(this.searchResult);
			}

			this.searchResult = document.querySelector("#people-observed-template").content.cloneNode(true).firstElementChild;
			this.searchResult.querySelector("h1").childNodes[0].nodeValue ="Search Result";
			this.searchResult.className = "search-results";

			// show results
			const resource = createResourceWithQueryParameters("/services/people", queryParameters);

			// query rest api
			let responsePeople = await fetch(resource, {
				method: "GET",
				headers: {"Accept": "application/json"},
				credentials: "include"
			});
			if (!responsePeople.ok) throw new Error("HTTP " + responsePeople.status + " " + responsePeople.statusText);
			const people = await responsePeople.json();

			let peopleReferences = [];
			for (const person of people)
				peopleReferences.push(person.identity);

			this.refreshAvatarSlider(this.searchResult.querySelector("span.slider"), peopleReferences, person => this.toggleObservation(person.identity));

			mainElement.appendChild(this.searchResult);
		}
	});


	/**
	 * Updates the session owner's observed people with the given person. Removes
	 * the given person if it is already observed by the owner, or adds it if not.
	 * @param {String} personIdentity the identity of the person to add or remove
	 */
	Object.defineProperty(PeopleController.prototype, "toggleObservation", {
		enumerable: false,
		configurable: false,
		value: async function (personIdentity) {
			if (personIdentity === Controller.sessionOwner.identity) return; // prevents you from adding yourself

			let peopleObservedReferences = Controller.sessionOwner.peopleObservedReferences;

			if (!peopleObservedReferences.includes(personIdentity)) {
				peopleObservedReferences.push(personIdentity)
			} else {
				const index = peopleObservedReferences.indexOf(personIdentity);
				if (index > -1) {
					peopleObservedReferences.splice(index, 1);
				}
			}

			// build content
			let contentList = [];
			for (let peopleObservedReference of peopleObservedReferences) {
				contentList.push("peopleObserved=" + peopleObservedReference);
			}
			const content = contentList.join("&");

			let resource = "/services/people/" + Controller.sessionOwner.identity + "/peopleObserved";

			let response = await fetch(resource, {
				method: "PUT",
				headers: {"Content-Type": "application/x-www-form-urlencoded"},
				body: content
			});
			if (!response.ok) throw new Error("HTTP " + response.status + " " + response.statusText);

			const uri = "/services/people/"+Controller.sessionOwner.identity;
			const responseSessionOwner = Controller.sessionOwner = await fetch(uri,{
				method :"GET",
				headers: {"Accept": "application/json"},
				credentials: "include"
			});

			if (!responseSessionOwner.ok) throw new Error("HTTP " + responseSessionOwner.status + " " + responseSessionOwner.statusText);
			Controller.sessionOwner = await responseSessionOwner.json();

			let html_people_observed = document.querySelector(".people-observed");
			let html_people_observing = document.querySelector(".people-observing");
			this.refreshAvatarSlider(html_people_observing.querySelector("span.slider"), Controller.sessionOwner.peopleObservingReferences, person => this.toggleObservation(person.identity));
			this.refreshAvatarSlider(html_people_observed.querySelector("span.slider"), Controller.sessionOwner.peopleObservedReferences, person => this.toggleObservation(person.identity));
		}
	});


	/**
	 * Perform controller callback registration during DOM load event handling.
	 */
	window.addEventListener("load", event => {
		const anchor = document.querySelector("header li:nth-of-type(3) > a");
		const controller = new PeopleController();
		anchor.addEventListener("click", event => controller.display());
	});
} ());