// Type declarations for @nednederlander/mvp-client
// This file provides TypeScript definitions for the MVP client package

declare module '@nednederlander/mvp-client' {
  export interface MessageDto {
    type: string;
    content: string;
    namespace: string;
  }

  export interface TabConfig {
    namespace: string;
    title: string;
    component: React.ComponentType<{ namespace: string }>;
    children?: TabConfig[];
    hooks?: TabLifecycleHooks;
    style?: React.CSSProperties;
  }

  export interface TabLifecycleHooks {
    onTabMount?: (namespace: string) => void;
    onTabUnmount?: (namespace: string) => void;
    onStateUpdate?: (namespace: string, state: any) => void;
  }

  export interface TabbedInterfaceProps {
    tabs: TabConfig[];
  }

  export const TabbedInterface: React.FC<TabbedInterfaceProps>;

  export interface SystemStateResult {
    state: any;
    loading: boolean;
    error: string | null;
    sendMessage: (message: Partial<MessageDto>) => Promise<MessageDto>;
  }

  export function useSystemState(namespace: string): SystemStateResult;
}