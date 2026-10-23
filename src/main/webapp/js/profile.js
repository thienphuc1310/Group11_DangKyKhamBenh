document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("profile-form");
    if (!form) {
        return;
    }

    const editButton = document.getElementById("edit-profile");
    const cancelButton = document.getElementById("cancel-profile");
    const viewActions = form.querySelector(".profile-view-actions");
    const editActions = form.querySelector(".profile-edit-actions");
    const controls = form.querySelectorAll("input, select, textarea");

    function setEditing(editing) {
        controls.forEach(function (control) {
            control.disabled = !editing;
        });
        viewActions.hidden = editing;
        editActions.hidden = !editing;

        if (editing) {
            const firstControl = form.querySelector("input, select, textarea");
            if (firstControl) {
                firstControl.focus();
            }
        }
    }

    editButton.addEventListener("click", function () {
        setEditing(true);
    });

    cancelButton.addEventListener("click", function () {
        form.reset();
        setEditing(false);
    });

    form.addEventListener("submit", function () {
        controls.forEach(function (control) {
            control.disabled = false;
        });
    });

    setEditing(form.dataset.editing === "true");
});
