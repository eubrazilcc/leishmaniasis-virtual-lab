/**
 * RequireJS module that defines the view: header->workspace.
 */

define([ 'marionette', 'tpl!apps/header/show/templates/workspace_header', 'flatui-checkbox', 'flatui-radio' ], function(Marionette, WorkspaceHeaderTpl) {

    return {
        Header : Marionette.ItemView.extend({
            id : 'workspace',
            template : WorkspaceHeaderTpl
        })
    };

});