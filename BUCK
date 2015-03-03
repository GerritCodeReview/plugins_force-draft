include_defs('//bucklets/gerrit_plugin.bucklet')

gerrit_plugin(
  name = 'force-draft',
  srcs = glob(['src/main/java/**/*.java']),
  resources = glob(['src/main/**/*']),
  manifest_entries = [
    'Gerrit-PluginName: forcedraft',
    'Gerrit-SshModule: com.googlesource.gerrit.plugins.forcedraft.ForceDraftSshModule',
    'Implementation-Title: Forcedraft plugin',
    'Implementation-URL: https://gerrit-review.googlesource.com/#/admin/projects/plugins/force-draft',
  ]
)

# TODO(davido): is this needed?
# requires for bucklets/tools/eclipse/project.py to work
# not sure, if this does something useful in standalone context
java_library(
  name = 'classpath',
  deps = [':force-draft__plugin'],
)
