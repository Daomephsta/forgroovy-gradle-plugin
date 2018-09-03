package daomephsta.forgroovy.gradleplugin

import org.gradle.api.Plugin
import org.gradle.api.Project

public class ForgroovyGradlePlugin implements Plugin<Project>
{
	@Override
	public void apply(Project project)
	{
		project.tasks.getByName('reobfJar').addPreTransformer(new IndyCallReobfuscatingTransformer())
		project.logger.info('Added IndyCallReobfuscatingTransformer to ForgeGradle pre-reobf transformers')
		//Apply groovy plugin
		project.plugins.apply('groovy')
		//Set -indy flag on compiler
		project.tasks.getByName('compileGroovy').groovyOptions.optimizationOptions.put('indy', true)
	}
}
