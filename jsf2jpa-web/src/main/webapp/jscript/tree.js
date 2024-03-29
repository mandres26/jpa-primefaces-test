/**
 * PrimeFaces Tree Widget
 */
PrimeFaces.widget.Tree = function(id, cfg) {
    this.id = id;
    this.cfg = cfg;
    this.jqId = PrimeFaces.escapeClientId(this.id);
    this.jq = $(this.jqId);
    this.cfg.formId = this.jq.parents('form:first').attr('id');

    if(this.cfg.selectionMode) {
        this.selectionHolder = $(this.jqId + '_selection');
        var selectionsValue = this.selectionHolder.val();
        this.selections = selectionsValue === '' ? [] : selectionsValue.split(',');

        if(this.cfg.selectionMode == 'checkbox')
            this.preselectCheckboxPropagation();
    }

    this.bindEvents();

    if(!this.cfg.dynamic) {
        this.cookieName = this.id + '_state';
        this.restoreClientState();
    }

    if(this.cfg.dragdrop) {
        this.setupDragDrop(this.jq.find('.ui-tree-node-label'), this.jq.find('.ui-tree-node-content'));
    }
}

PrimeFaces.widget.Tree.prototype.bindEvents = function() {
    var _self = this,
    selectionMode = this.cfg.selectionMode;

    //expand-collapse
    $(this.jqId + ' .ui-tree-icon')
        .die()
        .live('click',function(e) {
            var icon = $(this),
            node = icon.parents('li:first');

            if(icon.hasClass('ui-icon-triangle-1-e'))
                _self.expandNode(node);
            else
                _self.collapseNode(node);
        });

    //selection hover
    if(selectionMode) {
        var clickTargetSelector = selectionMode == 'checkbox' ? this.jqId  + ' .ui-tree-checkbox-box' : this.jqId  + ' .ui-tree-node-content';

        $(clickTargetSelector)
            .die()
            .live('mouseover', function() {
                $(this).addClass('ui-state-hover');
            })
            .live('mouseout', function() {
                $(this).removeClass('ui-state-hover');
            })
            .live('click', function(e) {
                _self.onNodeClick(e, $(this).parents('li:first'));
            });
    }
}

PrimeFaces.widget.Tree.prototype.onNodeClick = function(e, node) {
    if($(e.target).is(':not(.ui-tree-icon)')) {
        if(this.cfg.onNodeClick) {
            this.cfg.onNodeClick.call(this, node);
        }

        if(this.isNodeSelected(node))
            this.unselectNode(node);
        else
            this.selectNode(node);
    }
}

PrimeFaces.widget.Tree.prototype.expandNode = function(node) {
    var _self = this;

    if(this.cfg.dynamic) {

        if(this.cfg.cache && node.children('.ui-tree-nodes').length > 0) {
            this.showNodeChildren(node);
            
            return;
        }

        var options = {
            source: this.id,
            process: this.id,
            update: this.id,
            formId: this.cfg.formId
        };

        if(this.cfg.hasExpandListener && this.cfg.onExpandUpdate) {
            options.update = options.update + ' ' + this.cfg.onExpandUpdate;
        }

        options.onsuccess = function(responseXML) {
            var xmlDoc = responseXML.documentElement,
            updates = xmlDoc.getElementsByTagName("update");

            for(var i=0; i < updates.length; i++) {
                var id = updates[i].attributes.getNamedItem("id").nodeValue,
                content = updates[i].firstChild.data;

                if(id == _self.id){
                    node.append(content);

                    if(_self.cfg.dragdrop) {
                        _self.setupDragDrop(node.find('.ui-tree-node-label'), node.find('.ui-tree-node-content'));
                    }
                    
                    _self.showNodeChildren(node);
                }
                else {
                    PrimeFaces.ajax.AjaxUtils.updateElement(id, content);
                }
            }

            return false;
        };

        var params = {};
        params[this.id + '_expandNode'] = _self.getNodeId(node);

        options.params = params;

        PrimeFaces.ajax.AjaxRequest(options);
    }
    else {
        this.showNodeChildren(node);
        this.saveClientState();
    }
}

PrimeFaces.widget.Tree.prototype.collapseNode = function(node) {
    var _self = this,
    icon = node.find('.ui-tree-icon:first'),
    lastClass = node.attr('class').split(' ').slice(-1),
    nodeIcon = icon.next(),
    iconState = this.cfg.iconStates[lastClass];

    icon.addClass('ui-icon-triangle-1-e').removeClass('ui-icon-triangle-1-s');

    if(iconState) {
        nodeIcon.removeClass(iconState.expandedIcon).addClass(iconState.collapsedIcon);
    }

    node.children('.ui-tree-nodes').hide();
        
    if(_self.cfg.dynamic) {
        if(!_self.cfg.cache) {
            $(node.children('.ui-tree-nodes')).remove();

            if(_self.cfg.hasCollapseListener) {
                _self.fireNodeCollapseEvent(node);
            }
        }
    }
    else {
        _self.saveClientState();
    }
}

PrimeFaces.widget.Tree.prototype.showNodeChildren = function(node) {
    var icon = node.find('.ui-tree-icon:first'),
    lastClass = node.attr('class').split(' ').slice(-1),
    nodeIcon = icon.next(),
    iconState = this.cfg.iconStates[lastClass];

    icon.addClass('ui-icon-triangle-1-s').removeClass('ui-icon-triangle-1-e');

    if(iconState) {
        nodeIcon.removeClass(iconState.collapsedIcon).addClass(iconState.expandedIcon);
    }

    node.children('.ui-tree-nodes').show();
}

PrimeFaces.widget.Tree.prototype.saveClientState = function() {
    var _self = this,
    expandedNodes = [];
    
    $(this.jq).find('li').each(function() {
        var node = $(this),
        icon = node.find('.ui-tree-icon:first');

        if(icon.hasClass('ui-icon-triangle-1-s')) {
            expandedNodes.push(node.attr('id'));
        }
    });

    PrimeFaces.setCookie(this.cookieName, expandedNodes.join(','));
}

PrimeFaces.widget.Tree.prototype.restoreClientState = function() {
    var expandedNodes = PrimeFaces.getCookie(this.cookieName);
    
    if(expandedNodes) {
        expandedNodes = expandedNodes.split(',');

        for(var i in expandedNodes) {
            var node = $(PrimeFaces.escapeClientId(expandedNodes[i]));

            this.showNodeChildren(node);
        }
    }
}

PrimeFaces.widget.Tree.prototype.selectNode = function(node) {

    if(this.isCheckboxSelection()) {
        this.toggleCheckbox(node, true);
    }
    else {
        if(this.isSingleSelection()) {
            //clean all selections
            this.selections = [];
            this.jq.find('.ui-tree-node-content.ui-state-highlight').removeClass('ui-state-highlight');
        }

        node.find('.ui-tree-node-content:first').addClass('ui-state-highlight');

        this.addToSelection(this.getNodeId(node));
    }

    this.writeSelections();

    if(this.cfg.instantSelect) {
        this.fireNodeSelectEvent(node);
    }
}

PrimeFaces.widget.Tree.prototype.unselectNode = function(node) {
    var nodeId = this.getNodeId(node);

    //select node
    if(this.isCheckboxSelection()) {
        this.toggleCheckbox(node, false);
    }
    else {
        node.find('.ui-tree-node-content:first').removeClass('ui-state-highlight');

        //remove from selection
        this.removeFromSelection(nodeId);
    }

    this.writeSelections();

    if(this.cfg.instantUnselect) {
        this.fireNodeUnselectEvent(node);
    }
}

PrimeFaces.widget.Tree.prototype.writeSelections = function() {    
    this.selectionHolder.val(this.selections.join(','));
}

PrimeFaces.widget.Tree.prototype.fireNodeSelectEvent = function(node) {
    var options = {
        source: this.id,
        formId: this.cfg.formId
    };

    options.process = this.cfg.onSelectProcess ? this.cfg.onSelectProcess : this.id;

    if(this.cfg.onSelectUpdate) {
        options.update = this.cfg.onSelectUpdate;
    }

    options.onstart = this.cfg.onSelectStart;
    options.oncomplete = this.cfg.onSelectComplete;

    var params = {};
    params[this.id + '_instantSelection'] = this.getNodeId(node);

    options.params = params;

    PrimeFaces.ajax.AjaxRequest(options);
}

PrimeFaces.widget.Tree.prototype.fireNodeUnselectEvent = function(node) {
    var options = {
        source: this.id,
        process: this.id,
        formId: this.cfg.formId
    };

    if(this.cfg.onUnselectUpdate) {
        options.update = this.cfg.onUnselectUpdate;
    }

    var params = {};
    params[this.id + '_instantUnselection'] = this.getNodeId(node);

    options.params = params;

    PrimeFaces.ajax.AjaxRequest(options);
}

PrimeFaces.widget.Tree.prototype.fireNodeCollapseEvent = function(node) {
    var options = {
        source: this.id,
        process: this.id,
        update: this.cfg.onCollapseUpdate,
        formId: this.cfg.formId
    };

    var params = {};
    params[this.id + '_collapseNode'] = this.getNodeId(node);

    options.params = params;

    PrimeFaces.ajax.AjaxRequest(options);
}

PrimeFaces.widget.Tree.prototype.getNodeId = function(node) {
    return node.attr('id').split('_node_')[1];
}

PrimeFaces.widget.Tree.prototype.isNodeSelected = function(node) {
    return $.inArray(this.getNodeId(node), this.selections) != -1;
}

PrimeFaces.widget.Tree.prototype.isSingleSelection = function() {
    return this.cfg.selectionMode == 'single';
}

PrimeFaces.widget.Tree.prototype.isCheckboxSelection = function() {
    return this.cfg.selectionMode == 'checkbox';
}

PrimeFaces.widget.Tree.prototype.addToSelection = function(nodeId) {
    this.selections.push(nodeId);
}

PrimeFaces.widget.Tree.prototype.removeFromSelection = function(nodeId) {
    this.selections = $.grep(this.selections, function(r) {
        return r != nodeId;
    });
}

PrimeFaces.widget.Tree.prototype.toggleCheckbox = function(node, check) {
    var _self = this;

    //propagate selection down
    node.find('.ui-tree-checkbox-icon').each(function() {
        var icon = $(this),
        nodeId = _self.getNodeId(icon.parents('li:first'));

        if(check) {
            if($.inArray(nodeId, _self.selections) == -1) {
                icon.addClass('ui-icon ui-icon-check');
            
                _self.addToSelection(nodeId);
            }
        }
        else {
            icon.removeClass('ui-icon ui-icon-check');

            _self.removeFromSelection(nodeId);
        }
    });

    //propagate selection up
    node.parents('li').each(function() {
        var parentNode = $(this),
        nodeId = _self.getNodeId(parentNode),
        icon = parentNode.find('.ui-tree-checkbox-icon:first'),
        checkedChildren = parentNode.children('.ui-tree-nodes').find('.ui-tree-checkbox-icon.ui-icon-check'),
        allChildren = parentNode.children('.ui-tree-nodes').find('.ui-tree-checkbox-icon');

        if(check) {
            if(checkedChildren.length == allChildren.length) {
                icon.removeClass('ui-icon ui-icon-minus').addClass('ui-icon ui-icon-check');

                _self.addToSelection(nodeId);
            } else {
                icon.removeClass('ui-icon ui-icon-check').addClass('ui-icon ui-icon-minus');
            }
        }
        else {
            if(checkedChildren.length > 0) {
                icon.removeClass('ui-icon ui-icon-check').addClass('ui-icon ui-icon-minus');
            } else {
                icon.removeClass('ui-icon ui-icon-minus');
            }

            _self.removeFromSelection(nodeId);
        }

    });
}

PrimeFaces.widget.Tree.prototype.setupDragDrop = function(draggables, droppables) {
    var _self = this;

    //make all labels draggable
    draggables.draggable({
        revert:'invalid',
        helper: 'clone',
        containment: this.jqId
    });

    //make all node contents droppable
    droppables.droppable({
        hoverClass: 'ui-state-hover',
        drop: function(event, ui) {
            var newParent = $(this).parents('li:first'),
            draggedNode = ui.draggable.parents('li:first'),
            newParentChildrenContainer = newParent.children('.ui-tree-nodes'),
            oldParent = null;

            //ignore self dragdrop
            if(newParent.attr('id') == draggedNode.attr('id')) {
                return;
            }

            //If newParent had no children before, make it a parent
            if(newParentChildrenContainer.length == 0) {
                newParent.append('<ul class="ui-tree-nodes ui-helper-reset ui-tree-child"></ul>')
                .removeClass('ui-tree-item').addClass('ui-tree-parent')
                .find('.ui-tree-node-content:first').prepend('<span class="ui-tree-icon ui-icon ui-icon-triangle-1-s"></span>');
            }

            //If old parent has no children left, make it a leaf
            if(draggedNode.siblings().length == 0) {
                oldParent = draggedNode.parents('li:first');
                newParent.children('.ui-tree-nodes').append(draggedNode);
                oldParent.removeClass('ui-tree-parent').addClass('ui-tree-item');
                oldParent.find('.ui-tree-icon:first').remove();
                oldParent.children('.ui-tree-nodes').remove();
            }
            else {
                //append droppedNode to newParent
                newParent.children('.ui-tree-nodes').append(draggedNode);
            }

            _self.fireDragDropEvent(draggedNode, newParent);
        }
    });
}

PrimeFaces.widget.Tree.prototype.fireDragDropEvent = function(draggedNode, newParent) {
    var options = {
        source: this.id,
        process: this.id,
        formId: this.cfg.formId
    };

    if(this.cfg.onDragdropUpdate) {
        options.update = this.cfg.onDragdropUpdate;
    }

    options.oncomplete = function(xhr, status, args) {
        draggedNode.hide().fadeIn('fast');
    }

    var params = {};
    params[this.id + '_dragdrop'] = true;
    params[this.id + '_draggedNode'] = this.getNodeId(draggedNode);
    params[this.id + '_droppedNode'] = this.getNodeId(newParent);

    options.params = params;

    PrimeFaces.ajax.AjaxRequest(options);
}

PrimeFaces.widget.Tree.prototype.preselectCheckboxPropagation = function() {
    this.jq.find('.ui-tree-checkbox-icon').not('.ui-icon-check').each(function() {
        var icon = $(this),
        node = icon.parents('li:first');

        if(node.find('.ui-tree-checkbox-icon.ui-icon-check').length > 0)
            $(this).addClass('ui-icon ui-icon-minus');
    });
}