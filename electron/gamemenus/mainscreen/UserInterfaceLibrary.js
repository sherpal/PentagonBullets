/**
User Interface Library for Pentagone Bullet

This file describes the functions that may be called to interact with the User Interface
**/

var UI = { // The User Interface Object
	memoryStack: {
		openedPage: undefined,
		mainPage: undefined,
		openedMenuPanel: undefined,
		titleZone: undefined,
		titleInitialHeight: undefined
	},
	/** Méthodes graphiques **/
	hideWithSlide: function( htmlElement ) {
		htmlElement.style.maxHeight = '0px'
		htmlElement.style.opacity = '0'
	},
	showWithSlide: function( htmlElement, height ) {
		htmlElement.style.maxHeight = height + 'px'
		htmlElement.style.opacity = '1'
	},
	turnElementActive: function( htmlElement ) {
		var className = htmlElement.className
		className = 'w3-blue-gray ' + className
		htmlElement.className = className
	},
	turnElementPassive: function( htmlElement ) {
		var className = htmlElement.className
		className = className.replace( /w3-blue-gray\s*/g, '' )
		htmlElement.className = className
	},
	freeze: function( htmlElement ) {
		htmlElement.disabled = true
	},
	unfreeze: function( htmlElement ) {
		htmlElement.disabled = false
	},
	/** Gestion de la zone de titre **/
	showPentagonBullet: function() {
		UI.memoryStack.titleZone.style.maxHeight = UI.memoryStack.titleInitialHeight+'px'
	},
	hidePentagonBullet: function() {
		UI.memoryStack.titleZone.style.maxHeight = '0'
	},
	/** classe Panel **/
	Panel: function( containerElement, actionButton, slidingElement ) {
		this.containerElement = containerElement
		this.actionButton = actionButton
		this.slidingElement = slidingElement
		this.slidingElementHeight = slidingElement.offsetHeight * 1.1
		this.initialize()
	},
	/** classe Menu Panel **/
	MenuPanel: function( containerElement, formElement ) {
		UI.Panel.call( this, containerElement, containerElement.getElementsByClassName('menu-button')[0], containerElement.getElementsByClassName('sliding')[0] )
		this.formElement = formElement
		var t = this
		this.actionButton.onclick = function() {
			if( UI.memoryStack.openedMenuPanel == undefined ) {
				t.open()
				UI.memoryStack.openedMenuPanel = t
			}
			else {
				if( UI.memoryStack.openedMenuPanel == t ) {
					t.close()
					UI.memoryStack.openedMenuPanel = undefined
				}
				else {
					UI.memoryStack.openedMenuPanel.close()
					t.open()
					UI.memoryStack.openedMenuPanel = t
				}
			}
		}
	},
	/** classe Page **/
	Page: function( containerElement ) {
		this.containerElement = containerElement
	},
	/** Alert box and Confirm Box **/
	showAlertBox: function( title, text, callback ) {
		UI.alertBox.getElementsByTagName('h1')[0].innerHTML = title
		UI.alertBox.getElementsByTagName('p')[0].innerHTML = text
		UI.alertBox.style.display = 'block'
		UI.alertBox.getElementsByClassName('w3-button')[0].onclick = function() {
			UI.hideAlertBox()
			if( callback != undefined && callback != null ) {
				callback()
			}
		}
	},
	hideAlertBox: function() {
		UI.alertBox.style.display = 'none'
	},
	showConfirmBox: function( title, text, callback ) {
		UI.confirmBox.getElementsByTagName('h1')[0].innerHTML = title
		UI.confirmBox.getElementsByTagName('p')[0].innerHTML = text
		UI.confirmBox.style.display = 'block'
		UI.confirmBox.getElementsByClassName('w3-button')[0].onclick = function() {
			UI.hideConfirmBox()
			if( callback != undefined && callback != null ) {
				callback(true)
			}
		}
		UI.confirmBox.getElementsByClassName('w3-button')[1].onclick = function() {
			UI.hideConfirmBox()
			if( callback != undefined && callback != null ) {
				callback(false)
			}
		}
	},
	hideConfirmBox: function() {
		UI.confirmBox.style.display = 'none'
	},
	/** Player et Jeu **/
	Player: function(
		rank,// - player rank
		name,// - player name
		properties
	) {
		this.rank = rank
		this.name = name
		this.properties = properties
	},
	Game: function( properties ) {
		this.properties = properties
	},
	statManager: {
		players: [],
		Property: function( name, value ) {
			this.name = name
			this.value = value
		},
		addStat: function( name, value ) {
			var tr = document.createElement('tr')
			var td
			td = document.createElement('td')
			td.innerHTML = name
			td.style.textAlign = 'left'
			td.style.paddingRight = '16px'
			tr.appendChild( td )
			td = document.createElement('td')
			td.innerHTML = value
			td.style.textAlign = 'left'
			tr.appendChild( td )
			return tr
		},
		gamePage: undefined,
		appendPlayer: function( player ) {
			// Delete border bottom of previous sliding element
			if( player.rank > 1 ) {
				UI.statManager.players[ player.rank - 2 ].slidingElement.style.borderBottomWidth = '0px'
			}
			// Construct HTML Code
			var sectionElement = document.createElement('section')
			sectionElement.className = 'menu-panel w3-content'
			var hElement = document.createElement('h1')
			hElement.className = 'long-bar-clickable'
			var span = document.createElement('span')
				span.innerHTML = '#' + player.rank
				span.className = 'floating-rank'
				hElement.appendChild( span )
			span = document.createElement('span')
				span.innerHTML = player.name
				span.className = 'player-name'
				hElement.appendChild( span )
			sectionElement.appendChild( hElement )
			var divElement = document.createElement('div')
			divElement.className = 'w3-content sliding stat-content'
			sectionElement.appendChild( divElement )
			
			var table = document.createElement('table')
			player.properties.forEach( function(property) {
				table.appendChild( UI.statManager.addStat( property.name, property.value ) )
			} )
			
			
			divElement.appendChild( table )
			UI.pages.playersStat.containerElement.appendChild( sectionElement )
			
			// Create Panel from HTML code
			UI.statManager.players[ player.rank - 1 ] = new UI.Panel( sectionElement, hElement, divElement )
			
			// Add Some Graphic Sugar
			UI.statManager.players[ player.rank - 1 ].callbackOnClose = function(){
				var nextSibling = UI.pages.playersStat.containerElement.getElementsByTagName('section')[ player.rank - 1 ]
				if( player.rank != UI.statManager.players.length ) {
					UI.statManager.players[ player.rank ].actionButton.style.borderTopWidth = '0px'
				}
			}
			UI.statManager.players[ player.rank - 1 ].callbackOnEventOpen = function(){
				var nextSibling = UI.pages.playersStat.containerElement.getElementsByTagName('section')[ player.rank - 1 ]
				if( player.rank != UI.statManager.players.length ) {
					UI.statManager.players[ player.rank ].actionButton.style.borderTopWidth = '1px'
				}
			}
		},
		appendGame: function( game ) {
			// Construct HTML Code
			var sectionElement = UI.pages.gameStat.containerElement
			var divElement = sectionElement.getElementsByTagName('div')[0]
			var hElement = sectionElement.getElementsByTagName('h1')[0]
			
			var table = document.createElement('table')
			game.properties.forEach( function(property) {
				table.appendChild( UI.statManager.addStat( property.name, property.value ) )
			} )
			
			
			divElement.appendChild( table )
			
			// Create Panel from HTML code
			var generalPanel = new UI.Panel( sectionElement, hElement, divElement )
			setTimeout( function() { generalPanel.open() }, 200 )
		}
	}
}

UI.Panel.prototype = {
	initialize: function() {
		this.slidingElement.style.maxHeight = '0px'
		this.slidingElement.style.position = 'static'
		this.isOpened = false
		var t = this
		this.actionButton.onclick = function() { t.onActionButtonClick() }
	},
	isOpened: false,
	open: function() {
		UI.showWithSlide( this.slidingElement, this.slidingElementHeight )
		UI.turnElementActive( this.actionButton )
		this.isOpened = true
		if( this.callbackOnEventOpen != undefined ) {
			this.callbackOnEventOpen()
		}
		if( this.callbackOnOpen != undefined ) {
			var t = this
			setTimeout( t.callbackOnOpen, 700 )
		}
	},
	close: function() {
		UI.hideWithSlide( this.slidingElement )
		UI.turnElementPassive( this.actionButton )
		this.isOpened = false
		if( this.callbackOnEventClose != undefined ) {
			this.callbackOnEventClose()
		}
		if( this.callbackOnClose != undefined ) {
			var t = this
			setTimeout( t.callbackOnClose, 700 )
		}
	},
	onActionButtonClick: function() {
		if( this.isOpened ) {
			this.close()
		}
		else {
			this.open()
		}
	},
	callbackOnEventClose: undefined,
	callbackOnEventOpen: undefined,
	callbackOnClose: undefined,
	callbackOnOpen: undefined
}


UI.MenuPanel.prototype = UI.Panel.prototype
UI.MenuPanel.prototype.bruteClose = function() {
    this.close()
    UI.memoryStack.openedMenuPanel = undefined
}
UI.Page.prototype = {
	quitButton: undefined,
	open: function() {
		if( UI.memoryStack.openedPage == undefined ) {
			if( this == UI.memoryStack.mainPage ) {
				UI.showPentagonBullet()
			}
			else {
				UI.hidePentagonBullet()
			}
			this.containerElement.style.position = 'static'
			this.containerElement.style.opacity = '1'
			UI.memoryStack.openedPage = this
		}
		else {
			UI.memoryStack.openedPage.hide()
			var t = this
			setTimeout( function() {
				UI.memoryStack.openedPage.detach()
				UI.memoryStack.openedPage = undefined
				t.open()
			}, 200 )
		}
	},
	hide: function() {
		this.containerElement.style.opacity = '0'
	},
	detach: function() {
		this.containerElement.style.position = 'absolute'
	}
}