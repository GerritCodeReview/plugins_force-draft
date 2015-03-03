gerrit_plugin(
  name = 'force-draft',
  srcs = glob(['src/main/java/**/*.java']),
  resources = glob(['src/main/resources/**/*']),
  manifest_entries = [
    'Gerrit-PluginName: force-draft',
    'Gerrit-SshModule: com.googlesource.gerrit.plugins.forcedraft.ForceDraftSshModule',
    'Implementation-Title: Force draft plugin',
    'Implementation-URL: https://gerrit-review.googlesource.com/#/admin/projects/plugins/force-draft',
  ]
)

# requires for bucklets/tools/eclipse/project.py to work
# not sure, if this does something useful in standalone context
java_library(
  name = 'classpath',
  deps = [':force-draft__plugin'],
)
