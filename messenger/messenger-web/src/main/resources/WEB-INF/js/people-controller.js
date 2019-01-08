/**
 * de_sb_messenger.PeopleController: messenger people controller.
 * Copyright (c) 2013 Sascha Baumeister
 */
"use strict";

function createResourceWithQueryParameters(resource, queryParameters) {
	var res = resource + "?"
	Object.keys(queryParameters).forEach(function(paramName) {
		if (queryParameters[paramName] != "") {
			res = res + paramName + "=" + queryParameters[paramName] + "&";
		}
	})
	return res.slice(0, -1) // remove last &
}

(function () {
	// imports
	const Controller = de_sb_messenger.Controller;

	// parameter names for person filter query
	const QUERY_PARAMETER_NAMES = Object.freeze(["email", "givenName", "familyName", "street", "city"]);


	/**
	 * Creates a new people controller that is derived from an abstract controller.
	 */
	const PeopleController = function () {
		Controller.call(this);
		this.searchResult = null;
	}
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
			const inputElements = document.querySelectorAll("section.candidates input")
			let queryParameters = {
				"email": inputElements[0].value.trim(),
				"forename": inputElements[1].value.trim(),
				"surname": inputElements[2].value.trim(),
				"street": inputElements[3].value.trim(),
				"city": inputElements[4].value.trim(),
			}

			let resource = createResourceWithQueryParameters("/services/people", queryParameters)

			// if (document.querySelector())

			// query rest api
			let people = JSON.parse(await this.xhr(resource, "GET", {"Accept": "application/json"}, "", "text"))
			let peopleReferences = []
			for (var key in people) {
				peopleReferences.push(people[key].identity)
			}

			const mainElement = document.querySelector("main");

			if (this.searchResult !== null) {
				mainElement.removeChild(this.searchResult);
			}

			this.searchResult = document.querySelector("#people-observed-template").content.cloneNode(true).firstElementChild;
			this.searchResult.getElementsByTagName("h1")[0].innerText = "Search Result"
			this.searchResult.className = "search-results"

			// show results
			this.refreshAvatarSlider(this.searchResult.querySelector("span.slider"), peopleReferences, person => this.toggleObservation(person.identity));

			mainElement.appendChild(this.searchResult)
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
			let peopleObservedReferences = Controller.sessionOwner.peopleObservedReferences;

			console.log("is person observed: " + peopleObservedReferences.includes(personIdentity))

			if (peopleObservedReferences.includes(personIdentity)) {
				peopleObservedReferences.push(personIdentity)
			} else {
				var index = peopleObservedReferences.indexOf(personIdentity);
				if (index > -1) {
					peopleObservedReferences.splice(index, 1);
				}
			}

			let content = "peopleObserved=2&peopleObserved=6"
			// TODO: make content right

			let resource = "/services/people/" + Controller.sessionOwner.identity + "/peopleObserved"

			fetch(resource, {method: "PUT", headers: {"Content-Type": "application/x-www-form-urlencoded"}, body: content})
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