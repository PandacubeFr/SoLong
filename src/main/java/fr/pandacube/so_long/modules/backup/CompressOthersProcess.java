package fr.pandacube.so_long.modules.backup;

import fr.pandacube.so_long.config.ConfigManager;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;

public class CompressOthersProcess extends CompressProcess {
	
	protected CompressOthersProcess(BackupManager bm, final String n) {
		super(bm, Type.OTHERS, n);
	}
	
	
	public FilenameFilter getFilenameFilter() {
		List<String> ignoreList = ConfigManager.getInstance().getDefaultConfig().backup_othersIgnore.get(name);
		if (ignoreList == null)
			return null;
		return new SourceFilenameFilter(ignoreList);
	}
	
	
	
	private static class SourceFilenameFilter implements FilenameFilter {
		private final List<String> exclude;
		
		public SourceFilenameFilter(final List<String> e) throws IllegalArgumentException {
			exclude = e;
		}
		
		@Override
		public boolean accept(final File dir, final String name) {
			final String nameLower = name.toLowerCase();
			
			for (String pattern : exclude) {
				if (nameLower.matches(pattern.toLowerCase()))
					return false;
			}
			
			return true;
		}
	}
	

	
	@Override
	public File getSourceDir() {
		return new File("root".equals(name) ? "." : name);
	}
	
	@Override
	protected void onCompressStart() { }
	
	@Override
	protected void onCompressEnd(boolean success) { }
	
	
}
