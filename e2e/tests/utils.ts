import { ChildProcess, execSync } from 'child_process';

export function isPortInUse(port: number): boolean {
  try {
    execSync(`lsof -i :${port}`, { stdio: 'pipe' });
    return true;
  } catch {
    return false;
  }
}

export function killProcess(proc: ChildProcess) {
  if (proc && !proc.killed) {
    try {
      process.kill(-proc.pid!, 'SIGTERM');
    } catch (e: any) {
      if (e.code !== 'ESRCH') {
        console.error(`Failed to kill process ${proc.pid}:`, e);
      }
    }
  }
}
