load("//tools/bzl:plugin.bzl", "gerrit_plugin")

gerrit_plugin(
    name = "force-draft",
    srcs = glob(["src/main/java/**/*.java"]),
    resources = glob(["src/main/resources/**/*"]),
    manifest_entries = [
        "Gerrit-PluginName: force-draft",
        "Gerrit-SshModule: com.googlesource.gerrit.plugins.forcedraft.ForceDraftSshModule",
        "Implementation-Title: Force draft plugin",
        "Implementation-URL: https://gerrit-review.googlesource.com/#/admin/projects/plugins/force-draft",
    ],
)
