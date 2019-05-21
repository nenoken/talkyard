/**
 * Copyright (c) 2019 Kaj Magnus Lindberg
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/// <reference path="../more-prelude.more.ts" />


//------------------------------------------------------------------------------
   namespace debiki2.users {
//------------------------------------------------------------------------------

const r = ReactDOMFactories;


export const GroupMembers = React.createFactory<any>(function(props) {
  //displayName: 'GroupMembers',

  const group: Group = props.user; // werid name, could fix
  const store: Store = props.store;
  const me = store.me;
  const builtInOrNotStaff = member_isBuiltIn(group) || !isStaff(me);
  let isGone;  // BUG should be useState instead, right.

  const [membersNullOrFalse, setMembers] = React.useState<Participant[] | null | false>(null);

  React.useEffect(() => {
    listAndSetMembers();
    return () => { isGone = true };
  }, [me.id]);

  function listAndSetMembers() {
    Server.listGroupMembers(group.id, setMembers);
  }

  if (membersNullOrFalse === null)
    return t.Loading;

  if (!membersNullOrFalse)
    return r.p({}, "May not list members."); // I18N

  const members: Participant[] = membersNullOrFalse;

  function showAddMembersDialog() {
    const currentMemberIds = members.map(m => m.id);
    pagedialogs.openAddPeopleDialog(currentMemberIds, (newIds: UserId[]) => {
      if (isGone) return;
      Server.addGroupMembers(group.id, newIds, listAndSetMembers);
    });
  }

  function removeMember(memberId: UserId) {
    Server.removeGroupMembers(group.id, [memberId], listAndSetMembers);
  }

  const addMembersButton = builtInOrNotStaff || member_isBuiltIn(group) ? null :
      Button({ className: 'e_AddMbrsB', onClick: () => showAddMembersDialog() },
        "Add Members");  // I18N

  const memberElems = members.map((m: Participant) => {
     return r.li({ key: m.id },
        LinkUnstyled({ to: UsersRoot + m.username },
          UserName({ user: m, store, makeLink: false, onClick: null })),
        builtInOrNotStaff ? null : utils.ModalDropdownButton({ title: "Manage ..." },  // I18N
          Button({ className: '', onClick: () => removeMember(m.id) },
            "Remove")));   // I18N
  });

  const cannotModifyInfo = !member_isBuiltIn(group) ? null :
      r.p({}, "This is a built-in group; it cannot be modified.");  // I18N

  const orNoMembers = members.length ? null :
      r.p({ className: 's_G_NoMbrs'}, "No members.");  // I18N

  return rFragment({},
    cannotModifyInfo,
    r.h3({}, "Group mebers"), // I18N
    addMembersButton,
    r.ul({ className: 's_G_Mbrs' }, memberElems),
    orNoMembers);
});


//------------------------------------------------------------------------------
   }
//------------------------------------------------------------------------------
// vim: fdm=marker et ts=2 sw=2 tw=0 fo=r list