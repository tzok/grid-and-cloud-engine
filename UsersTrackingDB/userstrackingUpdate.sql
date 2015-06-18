-- --------------------------------------------------------

--
-- Struttura della tabella `ActiveJobCollections`
--

CREATE TABLE IF NOT EXISTS `ActiveJobCollections` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `common_name` varchar(100) NOT NULL,
  `description` varchar(100) NOT NULL,
  `task_counter` int(11) NOT NULL,
  `user_email` varchar(100) DEFAULT NULL,
  `start_timestamp` datetime DEFAULT NULL,
  `status` varchar(20) DEFAULT NULL,
  `end_timestamp` datetime DEFAULT NULL,
  `output_path` varchar(300) NOT NULL,
  `id_final_job` int(11) DEFAULT NULL,
  `collection_type` varchar(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Struttura della tabella `JobCollections`
--

CREATE TABLE IF NOT EXISTS `JobCollections` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `common_name` varchar(100) NOT NULL,
  `description` varchar(100) NOT NULL,
  `task_counter` int(11) NOT NULL,
  `start_timestamp` datetime DEFAULT NULL,
  `status` varchar(20) DEFAULT NULL,
  `end_timestamp` datetime DEFAULT NULL,
  `collection_type` varchar(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 ;

-- --------------------------------------------------------

--
-- Struttura della tabella `JobDescription`
--

CREATE TABLE IF NOT EXISTS `JobDescription` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `jobId` varchar(1000) DEFAULT NULL,
  `executable` varchar(100) NOT NULL,
  `arguments` varchar(300) DEFAULT NULL,
  `output` varchar(300) DEFAULT NULL,
  `error` varchar(300) DEFAULT NULL,
  `queue` varchar(300) DEFAULT NULL,
  `file_transfer` varchar(300) DEFAULT NULL,
  `total_cpu` varchar(4) DEFAULT NULL,
  `SPDM_variation` varchar(300) DEFAULT NULL,
  `number_of_processes` varchar(4) DEFAULT NULL,
  `JDL_requirements` text,
  `output_path` varchar(300) DEFAULT NULL,
  `input_files` text,
  `output_files` text,
  `proxy_renewal` char(1) DEFAULT NULL,
  `resubmit_count` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 ;

ALTER TABLE ActiveGridInteractions ADD id_job_collection int(11) DEFAULT NULL after e_token_server;
ALTER TABLE ActiveGridInteractions modify grid_id varchar(1000);
ALTER TABLE GridInteractions ADD id_job_collection int(11) DEFAULT -1 after timestamp_endjob;
ALTER TABLE JobDescription modify arguments text;
