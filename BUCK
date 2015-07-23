include_defs('//bucklets/gerrit_plugin.bucklet')

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

java_library(
  name = 'classpath',
  deps = GERRIT_PLUGIN_API + [':force-draft__plugin'],
)
